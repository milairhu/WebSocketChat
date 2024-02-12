
import {FunctionComponent} from 'react';
import "../static/css/chat.css"
import {useState, useRef, useEffect} from "react";
import {Chat} from "../models/Chat";
import API from '../api';
import { AxiosError } from 'axios';
import {useNavigate} from "react-router-dom";

interface NewChatProps{
}

const NewChat: FunctionComponent<NewChatProps> = () => {

    const [isCreated, setIsCreated] = useState<boolean>(false);
    const [alreadyExists, setAlreadyExists] = useState<boolean>(false);
    const navigate = useNavigate(); //Pour retourner à la page de login
    const [userId, setUserId] = useState<number>(0);
    const [newChat, setNewChat] = useState<Chat>({
        id: 0,
        title: '',
        description: '',
        duration: 1*60 + 15, //Relatif à la valeur par défaut mise dans les Inputs
        date: "",
        ownerId: 0
    });

    const refHours = useRef<HTMLInputElement>(null);
    const refMinutes = useRef<HTMLInputElement>(null);

    useEffect(() => {
        console.log("Ouvre NewChats")
        //Checke si la session est encore ouverte
        if(localStorage.getItem("userId") === null){
            navigate("/login");
            return;
        } else {
            setUserId(Number(localStorage.getItem("userId")))
        }
    } , []);

    useEffect(() => {
        setNewChat({...newChat, ownerId: userId})
    },[userId])

    function handleInputChange (event: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement> ) {
                const { name, value } = event.target;
                setNewChat({ ...newChat, [name]: value });
              }

    function handleDurationChange(){
                if(refMinutes.current && refHours.current){
                    const hours = parseInt(refHours.current?.value);
                   const minutes  = parseInt(refMinutes.current?.value);
                   const total : number = hours*60+ minutes;
                   setNewChat({ ...newChat, duration:total });
                }
    }

    function handleDateTimeChange(event: React.ChangeEvent<HTMLInputElement>){
         let date : string = event.target.value;
         //Ajout des secondes :
         date = date+":00";
         //On supprime le T médian entre date et heure et on remplace par un espace
         date = date.replace("T", " ");
        setNewChat({...newChat, date: date});
        }

    function sendForm(event: React.FormEvent) {
         event.preventDefault();
         API.post(`chats`, newChat)
                .then((res)=>{
                console.log("Chat créé : ", res.data)
                setIsCreated(true);
                setAlreadyExists(false);
                    })
                .catch((e : AxiosError) => {
                    console.log("Requete put: ", `/chats`);
                    console.log(e);
                    if (e.response?.status === 409){
                        //Un chat porte déjà ce nom
                        setAlreadyExists(true);
                        setIsCreated(false);
                    }
                })
    }

    return (
        <main className="d-flex flex-column w-100 h-100 justify-content-between ">
            <div className="w-100 text-center bg-white">Création d'un nouveau chat</div>
            {
                isCreated &&
                <div className="validation-card align-self-center">
                                Chat créé avec succès.
                </div>
            }
            {
                alreadyExists &&
                <div className="error-card align-self-center">
                                Un chat porte déjà ce nom. Veuillez ressaisir un nom.
                </div>
            }
            <div className="form-card bg-white align-self-center d-flex flex-column overflow-auto">
                <form onSubmit={sendForm} >
                    <div className="mb-3">
                        <label htmlFor="title" className="form-label">Nom</label>
                        <input type="text"  className="form-control" id="title" minLength={2}
                               maxLength={40} name="title" placeholder="Saisir un nom de salon" required
                               onChange={handleInputChange}/>
                    </div>
                    <div className="mb-3">
                        <label htmlFor="description" className="form-label">Description</label>
                        <textarea  className="form-control" id="description"
                               maxLength={50} name="description" placeholder="Saisir une description"
                               onChange={handleInputChange}/>
                    </div>
                    <div className="mb-3">
                        <div>
                            Durée
                        </div>
                        <div className="d-flex justify-content-around">
                            <div>
                                <label htmlFor="hours" className="form-label">Heures</label>
                                <input type="number" ref={refHours} className="form-control" id="hours" defaultValue={1} min={0}
                                       max={23} name="hours"
                                        onChange={handleDurationChange}/>
                            </div>
                            <div>
                                <label htmlFor="minutes" className="form-label">Minutes</label>
                                <input type="number" ref={refMinutes} className="form-control" defaultValue={15} id="minutes" min={0}
                                       max={59} name="minutes"
                                        onChange={handleDurationChange}/>
                            </div>
                        </div>
                    </div>
                    <div className="mb-3">
                        <label htmlFor="date" className="form-label">Date et Heure</label>
                        <input type="datetime-local" className="form-control" id="date" name="date" required
                        onChange={handleDateTimeChange}/>
                    </div>
                    <div className="d-flex justify-content-center">
                        <button className="align-middle btn btn-success" type="submit">Valider</button>
                    </div>
                </form>
            </div>
            <div/>
        </main>
    );
};

export default NewChat;
