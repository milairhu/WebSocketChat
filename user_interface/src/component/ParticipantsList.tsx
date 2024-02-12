
import { FunctionComponent } from 'react';
import "../static/css/chat.css"
import {ReactComponent as DotSVG} from "../assets/icons/dot.svg";
import {User} from "../models/User";
import API from '../api';
import {useState, useEffect} from "react";

interface ParticipantsListProps{
    chatId: number
    userId: number
    connectedUsers: number[]
}

const ParticipantsList: FunctionComponent<ParticipantsListProps> = ({chatId, userId, connectedUsers}) => {

    const [participants, setParticipants] = useState<User[]>([]);

    useEffect(() => {
        if(chatId !== 0 ){
            API.get(`chats/${chatId}/users`)
                .then((res)=>{
                    setParticipants(res.data);
                    console.log(`GET chats/${chatId}/users`)
                })
                .catch((e : Error) => {
                    console.log(`ERREUR GET chats/${chatId}/users`)
                })
        }

    }, [chatId])

    return (


        <aside style={{borderTopLeftRadius: '30px', borderBottomLeftRadius: '30px', paddingBottom: '1rem'}}>
            <div className="w-100 text-center bg-white ">Participants</div>
            <div className="w-100 h-100 overflow-auto">
                {
                    participants?.map((user : User) => (
                            <div key={user.id} className="chat-card-white shadow-sm">
                                {
                                    user.id===userId || connectedUsers.includes(user.id) ?
                                        <DotSVG fill="lightgreen" className="dotSvg align-self-center" />
                                        :
                                        <DotSVG fill="grey" className="dotSvg align-self-center" />
                                }
                                <span>{user.familyName}</span>
                                <span>{user.firstName}</span>
                            </div>
                        )
                    )
                }

            </div>

        </aside>
    );
};

export default ParticipantsList;
