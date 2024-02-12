import {FunctionComponent, useState} from 'react';
import "../../static/css/login.css"
import API from '../../api';
import {AxiosError} from "axios";
import Main from "../Main";
import {useNavigate} from 'react-router-dom';

interface LoginInterface {
}

const Login: FunctionComponent<LoginInterface> = ({}) => {

    const [mail, setMail] = useState('')
    const [password, setPassword] = useState('')
    const [userFalse, setUserFalse] = useState<boolean>(false);
    const [userIsDeactivated, setUserIsDeactivated] = useState<boolean>(false);
    const [userConnected, setUserConnected] = useState<boolean>(false);
    const navigate = useNavigate(); //Pour retourner à la page de login

    const handleLogin = (event : React.FormEvent) => {
        event.preventDefault();
        const data = {email: mail, password: password};
        API.post(`users/login`, data)
            .then((res) => {
                //l'utilisateur peut se connecter
                console.log("Bienvenue : ", res.data.firstName)
                setUserFalse(false);
                setUserIsDeactivated(false);
                if(res.data.isAdmin){
                    //Rediriger vers localhost 8080
                    window.location.href = 'http://localhost:8080/admin/users';

                } else {
                    //On enregistre l'ID de l'utilisateur dans le localStorage
                    localStorage.setItem('userId', res.data.id)
                    // On enregistre le bearer token dans le localStorage
                    localStorage.setItem('token', res.headers.authorization);
                    //On rentre dans l'application
                    setUserFalse(false);
                    setUserIsDeactivated(false);
                    setUserConnected(true);
                }
            })
            .catch((e: AxiosError) => {
                console.log("ERROR GET ", `users/login`);
                console.log(e);
                if(e.response?.status === 404){
                    //L'email rentré ou le mot de passe de convient pas
                    setUserFalse(true);
                    setUserIsDeactivated(false);
                }
                if(e.response?.status === 417){
                    // l'utilisateur est désactivé
                    setUserFalse(false);
                    setUserIsDeactivated(true);
                }
            })
    }

    return (
        <>
            {
            userConnected  ?
              navigate("/chats")

                        :

                        <div className="login-container justify-content-between">
                            <h1 className="mt-3">
                                Application de chats
                            </h1>
                            <form>
                                {
                                    userIsDeactivated &&
                                    <div className="text-danger text-center">
                                        Ce compte a été bloqué.
                                    </div>
                                }

                                <div className="mb-3">
                                    <label htmlFor="mail" className="form-label">Email</label>
                                    <input type="email" name="mail" className="form-control" id="mail" value={mail}
                                           onChange={e => {
                                               setMail(e.target.value)
                                           }} required={true}/>
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="password" className="form-label">Password</label>
                                    <input type="password" name="password" className="form-control" id="password" value={password}
                                           onChange={e => {
                                               setPassword(e.target.value)
                                           }} required={true}/>
                                    {
                                        userFalse &&
                                        <div className="text-danger ">Login ou mot de passe incorrect</div>
                                    }
                                </div>
                                <a className="" onClick={() => navigate("/login/forgotten")}>Mot de passe oublié?</a>
                                <button type="submit" className="btn btn-primary w-100 mt-3" onClick={handleLogin}>Connexion</button>
                            </form>
                            <div/>
                        </div>
            }

        </>

    );
};

export default Login;
