import {FunctionComponent, useEffect, useRef, useState} from 'react';
import "../static/css/chat.css"
import API from '../api';
import {AxiosError} from 'axios';
import {Chat} from "../models/Chat";
import {ReactComponent as ArrowSVG} from "../assets/icons/arrow.svg";
import {ReactComponent as DeleteSVG} from "../assets/icons/delete.svg";
import Select from 'react-select';
import {User} from "../models/User";
import {isChatOpen} from "../static/js/isChatOpen";
import {useNavigate} from "react-router-dom";
import {useParams} from "react-router-dom";

const EditChat: FunctionComponent<{}> = () => {

    const [chat, setChat] = useState<Chat>();
    const [updatedChat, setUpdatedChat] = useState<Chat>({
         id: 0,
         title: '',
         description: '',
         date: '',
         duration: 0,
         ownerId: 0
       });

    const [users, setUsers] = useState<User[]>([]);
    const [participants, setParticipants] = useState< readonly { value: number; label: string; }[]>([]);
    const [selectedParticipants, setSelectedParticipants] = useState< number[]>([]);
    const [isUpdated, setIsUpdated] = useState<boolean>(false);
    const [alreadyExists, setAlreadyExists] = useState<boolean>(false);


    const refHours = useRef<HTMLInputElement>(null);
    const refMinutes = useRef<HTMLInputElement>(null);
    const refSelect= useRef(null);

    const [userId, setUserId] = useState<number>(0);
    const navigate = useNavigate();
    const { chatId } = useParams();

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
        setUpdatedChat({...updatedChat, ownerId: userId})
    },[userId])

    useEffect(() => {
        if(Number(chatId) !== 0 && userId !== 0){
            API.get(`/chats/${chatId}`)
                //Récupère les infos du chat
                .then((res) => {
                    setChat(res.data);
                    setUpdatedChat(res.data);
                })
                .catch((e) =>{
                    console.error("ERROR GET /chats/"+chatId+" : "+e);
                })
            API.get("users")
                //Récupère les utilisateurs pouvant participer au chat : les non Admin
                .then((res) => {
                    //On enlève l'utilisateur lui même de la liste
                    setUsers(res.data.filter((user: User) => userId !== user.id))
                })
                .catch((e) => {
                    console.error("ERROR GET /users : " +e);
                })
            API.get(`/chats/${chatId}/invited`)
                //Récupère les utilisateurs déjà invités au chat
                .then((res) => {
                        setParticipants(res.data.map((u: User) => {
                            return {value: u.id, label: u.firstName + " " + u.familyName}
                        }));
                    }
                )
                .catch((e) => {
                    console.error("ERROR GET /users : " +e);
                })
        }
    } , [chatId, userId]);

    useEffect(() =>{
        setSelectedParticipants(participants.map((p) => p.value))
    }, [participants])

    function handleInputChange (event: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement> ) {
        const { name, value } = event.target;
        setUpdatedChat({ ...updatedChat, [name]: value });
    }

    function handleDurationChange(){
        if(refMinutes.current && refHours.current){
            const hours = parseInt(refHours.current?.value);
            const minutes  = parseInt(refMinutes.current?.value);
            const total : number = hours*60+ minutes;
            setUpdatedChat({ ...updatedChat, duration:total });
        }
    }

    function handleDateTimeChange(event: React.ChangeEvent<HTMLInputElement>){
        let date : string = event.target.value;
        //Ajout des secondes :
        date = date+":00";
        //On supprime le T médian entre date et heure et on remplace par un espace
        date = date.replace("T", " ");
        setUpdatedChat({...updatedChat, date: date});
    }

    function sendForm(event: React.FormEvent) {
        event.preventDefault();
        API.put(`chats/${chatId}`, updatedChat)
            .then((res)=>{
                // Et on met à jour la table des invitations
                API.post(`chats/${chatId}/invitations`, selectedParticipants)
                    .then((res) => {
                        //On met à jour la liste de participants
                        API.get(`/chats/${chatId}/invited`)
                            //Récupère les utilisateurs déjà invités au chat
                            .then((res) => {
                                    setParticipants(res.data.map((u: User) => {
                                        return {value: u.id, label: u.firstName + " " + u.familyName}
                                    }));
                                }
                            )
                            .catch((e) => {
                                console.error(`ERREUR GET/chats/${chatId}/invited`);
                            })
                    })
                    .catch((e) => {
                        console.log(`ERREUR POST chats/${chatId}/invitations : ${e}`)
                    })
                setIsUpdated(true);
                setAlreadyExists(false);
            })
            .catch((e : AxiosError) => {
                console.log("Requete put: ", `/chats`);
                console.log(e);
                if (e.response?.status === 409){
                    //Un chat porte déjà ce nom
                    setAlreadyExists(true);
                    setIsUpdated(false);
                }
            })
    }

    function toSelectItem(item: User): { value: number; label: string; } {
        return { value: item.id, label: item.firstName + " " + item.familyName };
    }

    function deleteParticipant(userId: number){
        API.delete(`/chats/${chatId}/${userId}`)
            .then(()=> {
                setParticipants(participants.filter(p => p.value !== userId))
            })
            .catch((e)=>{
                console.error("ERROR DELETE /chats/"+chatId+"/"+userId +" " +e);
            })
    }
    function deleteChat(event: React.FormEvent){
        event.preventDefault();
        console.log("Supprime chat "+ chatId);
        API.delete(`/chats/${chatId}`)
            .then(()=> {
                console.log("Chat supprimé ")
                navigate("/chats")
            })
            .catch((e)=>{
                console.error("ERROR DELETE /chats/"+chatId+" " +e);
            })
    }

    return (
        <main className="d-flex flex-column w-100 h-100 justify-content-start ">
            <div className="w-100 text-center bg-white">Modification d'un chat</div>
            <div className="d-flex flex-column w-100 h-100 mt-2 overflow-auto ">
                <div className="d-flex w-100 justify-content-between">
                    <div className="back align-self-start" style={{width: "10%"}} onClick={() => navigate("/chats")}>
                        <ArrowSVG fill="cornflowerblue" className="arrowSvg me-2" />
                        <div>Retour</div>
                    </div>
                    {
                        isUpdated ?
                        <div className="validation-card align-self-center my-2">
                            Chat modifié avec succès.
                        </div>
                        :
                            alreadyExists ?
                            <div className="error-card align-self-center my-2">
                                Un chat porte déjà ce nom. Veuillez ressaisir un nom.
                            </div>
                                :
                                <div/>
                    }
                    <div style={{width: '10%'}}/>
                </div>
                <div className="d-flex justify-content-start w-100 justify-content-around ps-5 pt-4 overflow-auto">
                        <div className="edit-chat-form-card bg-white align-self-start mb-2">
                            <form onSubmit={sendForm} >
                                <div className="mb-3">
                                    <label htmlFor="title" className="form-label">Nom</label>
                                    <input type="text"  className="form-control" id="title" minLength={2}
                                           maxLength={40} name="title" placeholder="Saisir un nom de salon" required
                                           defaultValue={chat?.title}
                                           onChange={handleInputChange}/>
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="description" className="form-label">Description</label>
                                    <textarea  className="form-control" id="description"
                                               maxLength={50} name="description" placeholder="Saisir une description"
                                               defaultValue={chat?.description}
                                               onChange={handleInputChange}/>
                                </div>
                                <div className="mb-3">
                                    <div>
                                        Durée
                                    </div>
                                    <div className="d-flex justify-content-around">
                                        <div>
                                            <label htmlFor="hours" className="form-label">Heures</label>
                                            <input type="number" ref={refHours} className="form-control" id="hours"  min={0}
                                                   max={23} name="hours"
                                                   defaultValue={chat && Math.floor(  chat?.duration / 60) }
                                                   onChange={handleDurationChange}/>
                                        </div>
                                        <div>
                                            <label htmlFor="minutes" className="form-label">Minutes</label>
                                            <input type="number" ref={refMinutes} className="form-control" id="minutes" min={0}
                                                   max={59} name="minutes"
                                                   defaultValue={chat && chat?.duration  % 60}
                                                   onChange={handleDurationChange}/>
                                        </div>
                                    </div>
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="date" className="form-label">Date et Heure</label>
                                    <input type="datetime-local" className="form-control" id="date" name="date" required
                                           onChange={handleDateTimeChange}
                                           defaultValue={chat?.date}/>
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="participants" className="form-label">Participants</label>
                                    <Select
                                        ref={refSelect}
                                        isMulti
                                        name="participants"
                                        menuPlacement="auto"
                                        isSearchable
                                        options={users.map(toSelectItem)}
                                        onChange={(opt) => {
                                            setSelectedParticipants(opt.map((p) =>  p.value));
                                            console.log("Participants : ",opt)
                                        }}
                                        placeholder="Saisissez la liste de participants"
                                    />
                                </div>
                                <div className="d-flex justify-content-center">
                                    <button className={`align-middle btn bg-danger text-white me-3 ${isChatOpen(chat) ? 'disabled' : ''}`} onClick={deleteChat}>
                                        Supprimer le chat
                                    </button>
                                    <button className="align-middle btn btn-success" type="submit">Valider</button>
                                </div>
                            </form>
                    </div>

                    <div className="suppress-card align-self-start  mb-2 mx-2">
                        <div className="text-center mb-2">
                            Participants
                        </div>
                        <div className="h-100 overflow-auto">
                            {
                                participants.length === 0 ?
                                    <div>
                                        Aucun participants à ce chat
                                    </div>
                                    :
                                    participants.map((participant) => (
                                        <div className="participant-suppress-card">
                                            {
                                                participant.label
                                            }
                                            <DeleteSVG className="deleteSvg" onClick={()=> deleteParticipant(participant.value)}/>
                                        </div>

                                    ))
                            }

                        </div>
                    </div>

                </div>

            </div>


        </main>
    );
};

export default EditChat;
