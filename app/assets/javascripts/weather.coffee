$ ->
  $('button').bind('click', (event) ->
    cityParam = $("input[name='city']").val()

    $("#msg").empty()
    $("#today-weathers").empty()
    $("#forecast-weathers").empty()

    $.ajax({
      contentType: "application/json; charset=utf-8",
      method: "POST",
      url: routes.controllers.MyApplication.getWeather().url,
      data: JSON.stringify({"city": cityParam}),
      dataType: "json"
    })
    .done((data) ->
      if(data.errNum == 0)
        date = $("<td>").addClass("text").text data.retData.today.date
        week = $("<td>").addClass("text").text data.retData.today.week
        curTemp = $("<td>").addClass("text").text data.retData.today.curTemp
        aqi = $("<td>").addClass("text").text data.retData.today.aqi
        fengxiang = $("<td>").addClass("text").text data.retData.today.fengxiang
        fengli = $("<td>").addClass("text").text data.retData.today.fengli
        hightemp = $("<td>").addClass("text").text data.retData.today.hightemp
        lowtemp = $("<td>").addClass("text").text data.retData.today.lowtemp
        type = $("<td>").addClass("text").text data.retData.today.type
        $("#today-weathers").append $("<tr>").append(date).append(week).append(curTemp).append(aqi).append(fengxiang).append(fengli).append(hightemp).append(lowtemp).append(type)

        $.each data.retData.forecast, (index, forecast) ->
          forecastDate = $("<td>").addClass("text").text forecast.date
          forecastWeek = $("<td>").addClass("text").text forecast.week
          forecastFengxiang = $("<td>").addClass("text").text forecast.fengxiang
          forecastFengli = $("<td>").addClass("text").text forecast.fengli
          forecastHightemp = $("<td>").addClass("text").text forecast.hightemp
          forecastLowtemp = $("<td>").addClass("text").text forecast.lowtemp
          forecastType = $("<td>").addClass("text").text forecast.type
          $("#forecast-weathers").append $("<tr>").append(forecastDate).append(forecastWeek).append(forecastFengxiang).append(forecastFengli).append(forecastHightemp).append(forecastLowtemp).append(forecastType)
      else
        $("#msg").append(data.errMsg))
    .fail((error) ->
      alert(error.status + ", " + error.statusText))
    .always()
  )