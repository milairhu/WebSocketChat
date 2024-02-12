
import {FunctionComponent, useEffect, useState} from 'react';
import "../static/css/chat.css"
import parameter from "../assets/icons/parameter.png"
import API from '../api';
import {Chat} from "../models/Chat";
import {isChatOpen} from "../static/js/isChatOpen";
import {useNavigate} from "react-router-dom";

interface ChatsListInterface {
    setEditMode: (chatId: number) => void
    setCurrentChat: (chatId: number) => void
}

const ChatsList: FunctionComponent<ChatsListInterface> = ({ setEditMode, setCurrentChat}) => {
    const [chats, setChats] = useState<Chat[]>([]);
    const [userId, setUserId] = useState<number>(0);
    const navigate = useNavigate();

    useEffect(() => {
        //Checke si la session est encore ouverte
        if(localStorage.getItem("userId") === null){
            navigate("/login");
            return;
        } else {
            setUserId(Number(localStorage.getItem("userId")))
        }
    } , []);

    useEffect(() => {
        if (userId !==0){
            API.get(`users/${userId}/chats`)
                .then((res) => {
                    setChats(res.data);
                })
                .catch((e: Error) => {
                    console.log("ERROR GET ", `users/${userId}/chats`);
                    console.log(e);
                })
        }
    }, [userId]);


    return (
        <aside className="h-100" style={{borderBottomRightRadius: '30px', borderTopRightRadius: '30px'}}>
            <div className="w-100 text-center bg-white ">Mes salons</div>
            <div className="w-100 h-100 overflow-auto">
            {
                chats?.map((chat: Chat) => (
                    <div key={chat.id} className={
                        `${isChatOpen(chat) ? 'chat-card-white' : 'chat-card-grey'} shadow-sm `}
                         onClick={
                             ()=> {
                                 if(isChatOpen(chat)){
                                     setCurrentChat(chat.id);
                                 }
                             }
                         }>
                        <span>{chat.title}</span>
                        {
                            userId === chat.ownerId &&
                            <img style={{width: '25px', alignSelf: 'end', height: '100%', cursor: "pointer"}} src={parameter} alt="Parametres"
                            onClick={()=> navigate("/chats/"+chat.id+"/edit")}/>
                        }
                    </div>
                    )
                )
            }
            </div>
        </aside>
    );
};

export default ChatsList;
