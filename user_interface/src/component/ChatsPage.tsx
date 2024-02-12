import {FunctionComponent, useEffect, useState} from 'react';
import "../static/css/chat.css"
import ChatsList from "./ChatsList";
import Messages from "./Messages";
import ParticipantsList from "./ParticipantsList";
import EditChat from "./EditChat";
import {useNavigate} from "react-router-dom";

const ChatsPage: FunctionComponent<{ currentWebSocket: WebSocket | null, setCurrentWebSocket: (websocket: WebSocket) => void}>
    = ({currentWebSocket, setCurrentWebSocket}) => {

    const [editMode, setEditMode] = useState<number>(0);
    const [chatId, setChatId] = useState<number>(0);
    const [connectedUsers, setConnectedUsers] = useState<number[]>([]);
    const [userId, setUserId] = useState<number>(0);
    const navigate = useNavigate(); //Pour retourner à la page de login

    useEffect(() => {
        //Checke si la session est encore ouverte
        if(localStorage.getItem("userId") === null){
            navigate("/login");
            return;
        } else {
            setUserId(Number(localStorage.getItem("userId")))
        }
    },[])


    useEffect(() => {
        //en changeant de chat, on réinitialise la liste de connectés à notre chat
        setConnectedUsers([userId])
    },[chatId])

    return (

            editMode === 0 ?
               ( <>
                    <ChatsList setEditMode={setEditMode} setCurrentChat={setChatId}/>
                    <Messages chatId={chatId} userId={userId}
                              connectedUsers={connectedUsers} setConnectedUsers={setConnectedUsers}
                    currentWebSocket={currentWebSocket} setCurrentWebSocket={setCurrentWebSocket}/>
                    <ParticipantsList chatId={chatId} userId={userId} connectedUsers={connectedUsers}/>
                </>)
                :
                <EditChat />


    );
};

export default ChatsPage;
