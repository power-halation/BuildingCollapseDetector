var WebSocketServer = require('ws').Server
var wss = new WebSocketServer({
	host : '0.0.0.0',
	port : 8000
});
var wc = [];
wss.on('connection', function(ws) {
	console.log("Connected");
	wc.push(ws);
	ws.on('close',function(){
		wc = wc.filter(function(v) {
				return v != ws;
		});
	});
	ws.on('message', function(message) {
		console.log('received: %s', message);
		if(message == "Android"){
			wc = wc.filter(function(v) {
				return v != ws;
			});
		} else {
			wc.forEach(function(client,index,array){
				try {
					client.send(message);
				} catch (e) {
					console.log(e.message);
				}
			});
		}
	});
});
