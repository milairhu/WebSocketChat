import {FunctionComponent, useState} from 'react';
import "../../static/css/login.css"
import API from '../../api';
import {AxiosError} from "axios";
import { useNavigate } from 'react-router-dom';

interface LoginForgottenInterface {
}

const LoginForgotten: FunctionComponent<LoginForgottenInterface> = () => {

    const [mail, setMail] = useState('')
    const [userFalse, setUserFalse] = useState<boolean>(false);
    const [ emailSent,setEmailSent ] =useState<boolean>(false);

    const navigate = useNavigate(); //Pour retourner à la page de login
    function sendMail(event: React.FormEvent) {
        event.preventDefault();
        const emailReceiver = {email: mail};
        API.post(`users/login/forgotten`, emailReceiver)
            .then((res) => {
                //l'utilisateur peut se connecter
                console.log("Email envoyé à : ", mail)
                setUserFalse(false);
                setEmailSent(true);
            })
            .catch((e: AxiosError) => {
                console.log("ERROR GET ", `users/login`);
                console.log(e);
                if(e.response?.status === 404){
                    //L'email rentré ou le mot de passe de convient pas
                    setUserFalse(true);
                    setEmailSent(false);
                }
            })
    }


    return (
        <div className="login-container text-center">
            <div className="forgotten-password-card">
                {
                    emailSent ?
                        <>
                            <h6 className="mb-3">
                                Un email contenant votre mot de passe a été envoyé à l'adresse email suivante :
                            </h6>
                            <span>{mail}</span>
                            <h6 className="mt-3">
                                Veuillez le modifier après votre prochaine connexion!
                            </h6>
                            <button className="w-25 align-self-center mt-3" type="button" onClick={() => navigate('/login')}>Retour</button>
                        </>
                        :
                        <>
                            <h4 className="mb-4">
                                Entrez votre adresse email pour récupérer votre mot de passe :
                            </h4>
                            <form className="d-flex flex-column" onSubmit={sendMail}>
                                <input className="w-50 align-self-center" type="email" id="email" name="email" placeholder="Saisir votre email" maxLength={50}
                                    required
                                    onChange={( event: React.ChangeEvent<HTMLInputElement>) => {
                                    setMail(event.target.value);
                                    } }/>
                                <div className="d-flex justify-content-center mt-2">
                                    <button className="ms-1" type="button" onClick={() => navigate('/login')}>Retour</button>
                                    <button className="ms-1" type="submit">Envoyer</button>
                                </div>

                                {
                                    userFalse &&
                                    <div className="text-danger mt-2 ">Aucun compte ne correspond à cette adresse email</div>
                                }
                            </form>
                        </>
                }

            </div>
        </div>

    );
};

export default LoginForgotten;
