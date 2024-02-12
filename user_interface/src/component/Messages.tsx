
import {FunctionComponent, useEffect, useRef, useState} from 'react';
import "../static/css/chat.css"
import API from '../api';
import {Chat} from "../models/Chat";

const Messages: FunctionComponent<
    {chatId: number, userId: number, connectedUsers: number[], setConnectedUsers: (users: number[]) =>void,
        currentWebSocket: WebSocket | null, setCurrentWebSocket: (webSocket: WebSocket) => void}>
    = ({chatId, userId, connectedUsers,
           setConnectedUsers,currentWebSocket, setCurrentWebSocket}) => {

    const refSendBar = useRef<HTMLInputElement>(null);
    const refMessages = useRef<HTMLDivElement>(null);

    const [chat, setChat] = useState<Chat>();
    const [jsonReceived, setJsonReceived] = useState<any>();

    useEffect(()=>{
        //Ferme l'ancienne websocket si existe, et en crée une nouvelle
            if(chatId !== 0 && userId !== 0) {
                if (currentWebSocket != null) {
                    console.log("Websocket ferme : ", currentWebSocket.url)
                    currentWebSocket.close();
                }
                setCurrentWebSocket(new WebSocket("ws://localhost:8080/WsChat/" + chatId + "/" + userId));
            }
        },[chatId])

    useEffect(() => {
        //Une fois que la websocket est créée, on intialise tous les paramètres associés
        console.log("Nouvelle webSocket : ", currentWebSocket?.url)

            //Obtient les infos du chat :
        if(chatId !== 0) {
            API.get(`chats/${chatId}`)
                .then((res) => {
                    setChat(res.data);
                })
                .catch((e) => {
                    console.log("Error chats/" + chatId + " : " + e)
                })

            //Met en place la webSocket
            currentWebSocket?.addEventListener("open", openFunc, false);
            currentWebSocket?.addEventListener("message", messageFunc, false);
            currentWebSocket?.addEventListener("close", closeFunc, false);
            if (refSendBar.current) {
                refSendBar.current.addEventListener("keyup", sendMessage, false)
            }
            if (refMessages.current) {
                refMessages.current.innerHTML = "";
                let newMessage = document.createElement('div');
                newMessage.className = "message-text-muted text-decoration-underline align-self-center mx-3 my-2 p-2";
                newMessage.innerHTML = "Bienvenu dans le chat!";
                refMessages.current.appendChild(newMessage);
            }
            return () => {
                currentWebSocket?.removeEventListener("open", openFunc, false);
                currentWebSocket?.removeEventListener("message", messageFunc, false);
                currentWebSocket?.removeEventListener("close", closeFunc, false);
                if (refSendBar.current) {
                    refSendBar.current.removeEventListener('keyup', sendMessage, false);
                }
            };
        }


    },[currentWebSocket])

    useEffect(() => {
        if (jsonReceived){
            console.log("Json recu : ", jsonReceived);
            if(jsonReceived.status === "onOpen" || jsonReceived.status === "onMessage") {
                setConnectedUsers(connectedUsers.filter((u) => u !== jsonReceived.senderId).concat(jsonReceived.senderId));
            }

            if(jsonReceived.status === "onClose"){
                //Un utilisateur s'est déconnecté, on l'enlève de la liste des utilisateurs connectés
                setConnectedUsers(connectedUsers.filter((u) => u !== jsonReceived.senderId));
            }
        }
    }, [jsonReceived])


    function openFunc() {
        console.log("Session ouverte cote client");
    }

    function  closeFunc() {
        console.log("Session fermée cote client");
    }

    function messageFunc (evt: any) {
        //Réception d'un message
        setJsonReceived(JSON.parse(evt.data))
        const messageRecu = JSON.parse(evt.data);

        if(messageRecu.status === "onMessage"){
            //le message reçu est un message envoyé par un utilisateur
            if (refMessages.current!=null){
                let messageContainer = document.createElement('div');
                let newMessage = document.createElement('div');
                newMessage.innerHTML = messageRecu.message;
                messageContainer.className = "align-self-start w-25 mb-3";
                newMessage.className = "message-received-card w-100 align-self-start mx-3 my-2 p-2 shadow-sm";
                let sender = document.createElement('div');
                sender.innerHTML = messageRecu.userName;
                sender.className = "sender text-muted ms-3 small font-italic";
                messageContainer.appendChild(newMessage);
                messageContainer.appendChild(sender);


                if (refMessages.current) {
                    refMessages.current.appendChild(messageContainer);
                }
                refMessages.current.scrollTop = refMessages.current.scrollHeight; //On scrolle en bas
            }
        }
    }



    function sendMessage(e : KeyboardEvent) {
        if (e.key === 'Enter' || e.keyCode === 13) {
            console.log("SendMessage()\n")
            if (refSendBar.current && refMessages.current){
                if (refSendBar.current.value !== null && refSendBar.current.value !== "") {
                    //Envoi du message
                    console.log("Envoie sur websocket : ", currentWebSocket?.url)
                    if(currentWebSocket){
                        currentWebSocket.send(refSendBar.current.value)
                    }

                    //On écrit le message chez nous
                    let newMessage = document.createElement('div');
                    newMessage.className = "message-sent-card align-self-end mx-3 my-2 bg-primary p-2 shadow-sm";
                    newMessage.innerHTML = refSendBar.current.value;
                    refMessages.current.appendChild(newMessage);
                    refMessages.current.scrollTop = refMessages.current.scrollHeight; //On scrolle en bas
                }
                //On vide la sendbar
                refSendBar.current.value = "";
            }

        }
    }



    return (
        <main>
            <div className="w-100 mb-2 text-center bg-white">{chat?.title}</div>
            {
                chatId === 0 ?
                    <div className="container d-flex align-items-center h-100 justify-content-center">
                        <div className="text-center">
                            <h2>Bienvenue !</h2>
                            <p>Rejoignez des salons de discussions pour pouvoir converser avec des utilisateurs </p>
                        </div>
                    </div>                    :
                     <>
                        <div id="messages" ref={refMessages} className="w-100 h-100 d-flex flex-column justify-content-start overflow-auto">
                        </div>
                        <div className="d-flex w-100 justify-content-center mt-2">
                             <input id="sendbar" ref={refSendBar} type="text" className="form-control w-75 mx-3 align-self-center "
                                    placeholder="Saisir votre message..." maxLength={500} />
                        </div>
                     </>
            }



        </main>
    );
};

export default Messages;
