import '../App.css';
import NewChat from "./NewChat";
import MyAccount from "./MyAccount";
import {FunctionComponent, useEffect, useState} from "react";
import ChatsPage from "./ChatsPage";
import {ReactComponent as LogoutSVG} from "../assets/icons/logout.svg";
import { useNavigate } from 'react-router-dom';

interface MainProps{
}

const Main: FunctionComponent<MainProps> = () => {

    const [showMessages, setShowMessages] = useState(true);
    const [showCreate, setShowCreate] = useState(false);
    const [showEdit, setShowEdit] = useState(false);
    const [currentWebSocket, setCurrentWebSocket] = useState<WebSocket | null>(null);
    const navigate = useNavigate(); //Pour retourner à la page de login

    useEffect(() => {
        //Checke si la session est encore ouverte
        if(localStorage.getItem("userId") === null){
            navigate("/login");
            return;
        }
    },[])

    function changePage(setter: Function) {
        setShowMessages(false);
        setShowCreate(false);
        setShowEdit(false);
        setter(true);
    }

    function logoutFunc() {
        localStorage.removeItem("userId");
        navigate("/login")
    }

    return (
        <>
            <header className="text-align-end">
                Application de chats
            </header>
            <div>
                <nav className=" d-flex justify-content-between navbar navbar-expand-lg navbar-light">
                    <div id="navbarSupportedContent">
                        <ul className="navbar-nav me-auto d-flex flex-row justify-content-around">
                            <li className="nav-item me-2">
                                <a className={`nav-link ${showMessages ? "active" : ""}`} aria-current="page" onClick={()=>changePage(/*"messages"*/ setShowMessages)}>Salon de discussions</a>
                            </li>
                            <li className="nav-item me-2">
                                <a className={`nav-link ${showCreate ? "active" : ""}`} aria-current="page"  onClick={()=> {
                                    changePage(/*"create"*/ setShowCreate);
                                    currentWebSocket?.close();
                                    setCurrentWebSocket(null);

                                }}>Nouveau salon</a>
                            </li>
                            <li className="nav-item">
                                <a className={`nav-link ${showEdit ? "active" : ""}`} aria-current="page" onClick={()=> {
                                    changePage(/*"create"*/ setShowEdit);
                                    currentWebSocket?.close();
                                    setCurrentWebSocket(null);}}
                                >Mon compte</a>
                            </li>
                        </ul>
                    </div>
                    <div className="logout h-100" onClick={()=> {
                        logoutFunc();
                        currentWebSocket?.close();
                        setCurrentWebSocket(null);
                    }}>
                        <div className="me-1 align-self-center">
                            Déconnexion
                        </div>
                        <LogoutSVG className="logoutSvg"/>
                    </div>
                </nav>
            </div>

            <div className="content">
                {
                    showMessages &&
                    <ChatsPage currentWebSocket={currentWebSocket} setCurrentWebSocket={setCurrentWebSocket}/>

                }
                {
                    showCreate  &&
                    <>
                        <NewChat />
                    </>

                }
                {
                    showEdit  &&
                    <>
                        <MyAccount />
                    </>

                }
            </div>
        </>
    );
}

export default Main;
