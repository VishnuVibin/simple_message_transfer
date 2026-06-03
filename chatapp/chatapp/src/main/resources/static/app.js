let stompClient = null;

const socket = new SockJS('/chat');

stompClient = Stomp.over(socket);

stompClient.connect({}, function () {

    stompClient.subscribe('/topic/messages',
        function (message) {

            const msg =
                JSON.parse(message.body);

            document.getElementById("messages")
                .innerHTML +=
                `<p><b>${msg.sender}:</b> ${msg.content}</p>`;
        });
});

function sendMessage() {

    const sender =
        document.getElementById("username").value;

    const content =
        document.getElementById("message").value;

    if(content.trim()===""){
        return;
    }

    stompClient.send(
        "/app/send",
        {},
        JSON.stringify({
            sender: sender,
            content: content
        })
    );

    document.getElementById("message").value="";
}