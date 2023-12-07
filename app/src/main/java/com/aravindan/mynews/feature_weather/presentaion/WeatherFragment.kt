package com.aravindan.mynews.feature_weather.presentaion

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aravindan.mynews.R
import com.aravindan.mynews.core.data.location.LocationResource
import com.aravindan.mynews.core.data.remote.Resource
import com.aravindan.mynews.core.util.Constants
import com.aravindan.mynews.core.util.MyUtil
import com.aravindan.mynews.core.util.PermissionUtlil
import com.aravindan.mynews.core.util.collectLatestLifecycleFlow
import com.aravindan.mynews.core.util.getData
import com.aravindan.mynews.core.util.loadImage
import com.aravindan.mynews.databinding.FragmentWeatherBinding
import com.aravindan.mynews.feature_weather.domain.WeatherUtlil
import com.aravindan.mynews.feature_weather.domain.model.ForecastDay
import com.aravindan.mynews.feature_weather.domain.model.GraphType
import com.aravindan.mynews.feature_weather.domain.model.Hour
import com.aravindan.mynews.feature_weather.domain.model.WeatherGraph
import com.aravindan.mynews.feature_weather.domain.model.WeatherRes
import com.aravindan.mynews.feature_weather.domain.model.WeatherTab
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class WeatherFragment : Fragment() {
    private var binding: FragmentWeatherBinding? = null
    private val viewModel: WeatherViewModel by viewModels()
    private var locationPermissionRequest: ActivityResultLauncher<Array<String>>? = null
    private var resolutionForLocationResult: ActivityResultLauncher<IntentSenderRequest>? = null
    private var forecastAdapter:ForecastAdapter?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFun()
        initResultLaunhcer()
        observerData()
    }

    private fun observerData() {
        collectLatestLifecycleFlow(viewModel.locations) {
            when (it) {
                is LocationResource.NoPermission -> {
                    showError(
                        getString(R.string.allow_location_alert),
                        getString(R.string.allow_access)
                    )
                }
                is LocationResource.LocationNotEnabled -> {
                    showError(
                        getString(R.string.allow_location_alert),
                        getString(R.string.enable_location)
                    )
                }
                is LocationResource.Success -> {
                    ///showLoader()
                }
                is LocationResource.Loading -> {
                    showLoader()
                }
                is LocationResource.Error -> {
                    showError(it.error, getString(R.string.retry))
                }
            }
        }
        collectLatestLifecycleFlow(viewModel.weatherInfoFlow){
            when(it){
                is Resource.Loading->{
                    showLoader()
                }
                is Resource.Success->{
                    if (it.data!=null){
                        showContent(it.data)
                    }
                }
                is Resource.Error->{
                    showError(it.error, getString(R.string.retry))
                }
            }
        }
    }

    private fun showLoader() {
        binding?.progressBar?.isVisible = true
        binding?.llyWeatherContent?.isVisible = false
        binding?.widgetLocation?.llyContent?.isVisible = false
    }

    @SuppressLint("SetTextI18n")
    private fun showContent(data: WeatherRes) {
        binding?.progressBar?.isVisible = false
        binding?.llyWeatherContent?.isVisible = true
        binding?.widgetLocation?.llyContent?.isVisible = false
        binding?.llyCurrentWeather?.apply {
            tvLocation.text="${data.location?.name}, ${data.location?.region}, ${data.location?.country}"
            setWeatherData()
            val (qualityResId,color)=WeatherUtlil.getAirQuality(data.current?.airQuality?.usEpaIndex)
            tvAirQuality.text= getString(qualityResId)
            viewAirIndexIndicator.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(),color))
            forecastAdapter?.submitList(data.forecast?.forecastday)
            setGraph()
        }
    }
    private fun setWeatherData(){
        val data= viewModel.weatherInfoFlow.value.getData() ?: return
        val calendar=Calendar.getInstance()
        val foreCast=viewModel.forecastDay.value
        if(foreCast==null || MyUtil.getCalender(foreCast.date,Constants.API_DATE_FORMAT2)?.get(Calendar.DATE)==calendar.get(Calendar.DATE)){
            currentWeather(data)
        }else{
            forecastWeather(foreCast)
        }
    }
    @SuppressLint("SetTextI18n")
    private fun forecastWeather(data: ForecastDay?){
        binding?.llyCurrentWeather?.apply {
            binding?.llyCurrentWeather?.tvDate?.text= MyUtil.getDateFromString(data?.date, Constants.FULL_DATE,Constants.API_DATE_FORMAT2)
            data?.day.let { day->
                imgCurrentWeather.loadImage("https:"+day?.condition?.icon)
                tvCondition.text=day?.condition?.text
                tvTemp.text="${day?.avgtempC}℃"
                tvPercip.text="${day?.totalprecipMm} mm"
                tvWind.text="${day?.maxwindKph} Km/h"
                tvHumidity.text="${day?.avghumidity}%"
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun currentWeather(data: WeatherRes){
        binding?.llyCurrentWeather?.apply {
            binding?.llyCurrentWeather?.tvDate?.text="${MyUtil.getDateFromString(MyUtil.getTodayDate(), Constants.FULL_DATE,Constants.API_DATE_FORMAT2)} | ${
                if (data.current?.isDay==1)getString(R.string.day) else getString(R.string.night)}"
            imgCurrentWeather.loadImage("https:"+data.current?.condition?.icon)
            tvCondition.text=data.current?.condition?.text
            tvTemp.text="${data.current?.tempC}℃"
            tvPercip.text="${data.current?.precipMm} mm"
            tvWind.text="${data.current?.windKph} Km/h"
            tvHumidity.text="${data.current?.humidity}%"
        }
    }
    private fun getGraphList(list: List<Hour?>?,type:GraphType):List<WeatherGraph>{
        val foreCast=viewModel.forecastDay.value
        val calendar=Calendar.getInstance()
        val foreCastHours=if (MyUtil.getCalender(foreCast?.date,Constants.API_DATE_FORMAT2)?.get(Calendar.DATE)==calendar.get(Calendar.DATE)){
            list?.filter { (MyUtil.getCalender(it?.time)?.get(Calendar.HOUR_OF_DAY) ?: 0) >= calendar.get(Calendar.HOUR_OF_DAY) }
        }else
            list

        val groupedLists = foreCastHours?.chunked(if (foreCastHours.size<7) 1 else 2)
        val hours= mutableListOf<WeatherGraph>()
        groupedLists?.forEach {
            val precipMm= it.sumOf { it?.precipMm?:0.00 }
            val tempC =  it.map { it?.tempC?:0.0 }.average()
            val centerIndex = it.size / 2
            val time = it[centerIndex]?.time
            hours.add(WeatherGraph(time, if (type==GraphType.TEMPERATURE) tempC else precipMm))
        }
      return hours
    }
    private fun setGraph(){
        if (viewModel.forecastDay.value==null) return
       val type=viewModel.graphType.value
        val hours= getGraphList(viewModel.forecastDay.value?.hour,type)
        binding?.lineChart?.apply {
            clearGraph()
            description.isEnabled=false
            setTouchEnabled(true)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            setDrawGridBackground(false)
            isDragEnabled=true
            setScaleEnabled(false)
            legend.isEnabled=false
            val set=LineDataSet(getEntries(hours),"")
            set.setCircleColor(ContextCompat.getColor(requireContext(),R.color.transparent))
            set.color = ContextCompat.getColor(requireContext(),
                if (type==GraphType.TEMPERATURE)R.color.yellow_dark else R.color.blue_dark )
            set.lineWidth=3f
            set.circleRadius=0f
            set.highLightColor=ContextCompat.getColor(requireContext(),R.color.purple_500)
            set.setDrawCircleHole(false)
            set.setDrawFilled(true)
            set.fillFormatter = object :IFillFormatter{
                override fun getFillLinePosition(
                    dataSet: ILineDataSet?,
                    dataProvider: LineDataProvider?
                ): Float {
                    return axisLeft.axisMinimum
                }
            }
            set.valueTextColor=ContextCompat.getColor(requireContext(),R.color.black)
            set.valueTextSize=11f
            set.valueFormatter=object :ValueFormatter(){
                override fun getFormattedValue(value: Float): String {
                    val measurement=if(type==GraphType.TEMPERATURE)"℃" else " mm"
                    return value.toInt().toString()+measurement
                }
            }
            val drawable=ContextCompat.getDrawable(requireContext(),
             if (type==GraphType.TEMPERATURE) R.drawable.yellow_gradinent else R.drawable.blue_gradinent)
            set.fillDrawable=drawable
            val mData=LineData(set)
            mData.setValueTextColor(ContextCompat.getColor(requireContext(),R.color.black))
            mData.setValueTextSize(11f)
            setData(mData)
            axisLeft.isEnabled=false
            axisRight.isEnabled=false
            axisLeft.axisMinimum=-5f
            axisRight.axisMinimum=-5f
            axisRight.axisMaximum=50f
            axisLeft.axisMaximum=50f
            xAxis.setDrawGridLines(false)
            xAxis.spaceMin = 0.4f
            xAxis.spaceMax=0.4f
            axisLeft.granularity=51f
            xAxis.position=XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter=object :ValueFormatter(){
                override fun getFormattedValue(value: Float): String {
                    return if(value<0) ""
                    else MyUtil.getDateFromString(hours.get(value.toInt()).time?:"")
                }
            }
            xAxis.setCenterAxisLabels(false)
            xAxis.granularity=1f
            data.isHighlightEnabled = false
            data.dataSets.forEach {iLineDataSet ->
                val sets=iLineDataSet as LineDataSet
                sets.mode=if (iLineDataSet.mode==LineDataSet.Mode.CUBIC_BEZIER)
                    LineDataSet.Mode.LINEAR else LineDataSet.Mode.CUBIC_BEZIER
            }
            setVisibleXRangeMaximum(8f)
            invalidate()
            animateX(1000)
        }
    }
    private fun getEntries(list: List<WeatherGraph?>?):List<Entry>{
        val entries= mutableListOf<Entry>()
        list?.forEachIndexed { index, hour ->
            val value=hour?.value?:0.00
            entries.add(Entry(index.toFloat(),value.toFloat()))
        }
        return entries
    }
    private fun LineChart?.clearGraph(){
        this?.apply {
            fitScreen()
            data?.clearValues()
            xAxis?.valueFormatter=null
            notifyDataSetChanged()
            clear()
            invalidate()
        }
    }
    private fun showError(error: String?, btnTxt: String) {
        binding?.progressBar?.isVisible = false
        binding?.llyWeatherContent?.isVisible = false
        binding?.widgetLocation?.apply {
            llyContent.isVisible = true
            tvMessage.text = error
            btnRetry.text = btnTxt
        }
    }
    private fun initFun() {
        binding?.apply {
            swipeRefresh.setOnRefreshListener {
                swipeRefresh.isRefreshing=false
                viewModel.getLocation()
            }
            widgetLocation.btnRetry.setOnClickListener {
                requestLocationPermission()
            }
            val types=listOf(
                WeatherTab(getString(R.string.temperature),GraphType.TEMPERATURE.id,true),
                WeatherTab(getString(R.string.precipitation),GraphType.PRECIPITATION.id),
            )
            types.forEach {
                it.isSelected = viewModel.graphType.value.id==it.id
            }
            recyclerTabs.adapter=WeatherTabAdapter(types){
                viewModel.graphType.value=if(it.id==GraphType.TEMPERATURE.id) GraphType.TEMPERATURE else GraphType.PRECIPITATION
                setGraph()
            }
            forecastAdapter= ForecastAdapter(){selected->
                viewModel.weatherInfoFlow.value.getData()?.forecast?.forecastday?.forEach {
                    it.isSelected = it==selected
                }
                forecastAdapter?.submitList(viewModel.weatherInfoFlow.value.getData()?.forecast?.forecastday)
                forecastAdapter?.notifyDataSetChanged()
                viewModel.forecastDay.value=selected
                setWeatherData()
                setGraph()
            }
            recyclerForeCast.itemAnimator=null
            recyclerForeCast.adapter=forecastAdapter
        }
    }

    private fun requestLocationPermission() {
        if (PermissionUtlil.hasLocationPermission(requireContext())) {
            if (PermissionUtlil.isLocationEnabled(requireContext()))
                viewModel.getLocation()
             else requestLocation()
        } else {
            locationPermissionRequest?.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun requestLocation() {
        try {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000
            ).build()
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(request)
            builder.setAlwaysShow(true)
            val task = LocationServices.getSettingsClient(requireActivity())
                .checkLocationSettings(builder.build())
            task.addOnSuccessListener(requireActivity()) {
                viewModel.getLocation()
            }
            task.addOnFailureListener(requireActivity()) { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                        resolutionForLocationResult?.launch(intentSenderRequest)
                    } catch (sendEx: Exception) {
                        sendEx.printStackTrace()
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun initResultLaunhcer() {
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->

            if (PermissionUtlil.hasLocationPermission(requireContext())) {
                requestLocationPermission()
            } else {
                if (PermissionUtlil.shouldShowRational(requireActivity(),result)){
                    showPermissionDeniedAlert()
                }
            }
        }
        resolutionForLocationResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
                if (activityResult.resultCode == RESULT_OK){
                    requestLocation()
                }
            }
    }
    private fun showPermissionDeniedAlert(){
        val alert= AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.permission_denied))
            .setCancelable(false)
            .setMessage( getString(R.string.permission_denied_alert))
            .setPositiveButton(getString(R.string.open_setting)) { dialog, which ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.no_thanks)) { dialog, which ->
                dialog.dismiss()
            }
        alert.show()

    }
}