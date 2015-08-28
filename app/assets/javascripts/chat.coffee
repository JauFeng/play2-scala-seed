$ ->
  wsUri = "ws://localhost:9000/chatroom/broadcast"
  websocket = new WebSocket(wsUri)

  websocket.onopen = (event) ->
    console.log("CONNECTED")

  websocket.onclose = (event) ->
    console.log("DISCONNECTED")

  websocket.onmessage = (event) ->
    console.log(event.data)
    broadcast = JSON.parse(event.data)
    $('#message-list').append($('<li class="list-group-item list-group-item-info">').append($('<span class="glyphicon glyphicon-user" aria-hidden="true">').append(broadcast.name + " says: ")).append $('<span class="default-color3">').append(broadcast.message))



  websocket.onerror = (event) ->
    console.log(event.data)

  $('#message-submit').bind('click', (event) ->
    json = JSON.stringify({
      "name": $('input[name="name"]').val(),
      "message": $('input[name="message"]').val()
    })
    websocket.send(json)
  )