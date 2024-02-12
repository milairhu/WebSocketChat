import {FunctionComponent, useEffect, useRef, useState} from 'react';
import "../static/css/chat.css"
import {generatePassword, isPasswordValid} from "../static/js/password.js"
import {User} from "../models/User";
import API from '../api';
import {AxiosError} from 'axios';
import {useNavigate} from "react-router-dom";

const MyAccount: FunctionComponent<{}> = () => {

    const [user, setUser] = useState<User>();
    const [updateDone, setUpdateDone] = useState<boolean>(false);
    const [emailError, setEmailError] = useState<boolean>(false);
    const [fieldError, setFieldError] = useState<boolean>(false);
    const [passwordError, setPasswordError] = useState<boolean>(false);
    const [updatedUser, setUpdatedUser] = useState<User>({
         id: 0,
         email: '',
         firstName: '',
         familyName: '',
         password: '',
         isAdmin: false,
         isDeactivated: false
       });
    const [userId, setUserId] = useState<number>(0);


    const refPasswordInput = useRef<HTMLInputElement>(null);
    const navigate = useNavigate(); //Pour retourner à la page de login

    useEffect(()=> {
        //Checke si la session est encore ouverte
        if(localStorage.getItem("userId") === null){
            navigate("/login");
            return;
        }
        setUserId(Number(localStorage.getItem("userId")))
    } ,[]);

    useEffect(() => {
        if(userId !== 0){
            API.get(`users/${userId}`)
                .then((res)=>{
                    setUser(res.data);
                    setUpdatedUser(res.data);
                    setPasswordError(!isPasswordValid(res.data.password));
                })
                .catch((e : Error) => {
                    console.log("Requete : ", `/users/${userId}`);
                    console.log(e);
                })
        }
    },[userId])

    function updateUser(event: React.FormEvent) {
        event.preventDefault();
        if(!isPasswordValid(updatedUser.password)){
            setPasswordError(true);
            setUpdateDone(false);
            setEmailError(false);
            setFieldError(false);
            return;
        }
         API.put(`users/${userId}`, updatedUser)
            .then((res)=>{
            console.log("Mise à jour user : ", res.data)
            setUpdateDone(true);
            setEmailError(false);
            setFieldError(false);
            setPasswordError(false);
            })
            .catch((e : AxiosError) => {
                console.log("Error Requete put: ", `/users/${userId}`);
                console.log(e);
                if(e.response?.status === 409){
                    //Email existe déjà
                    setEmailError(true);
                    setUpdateDone(false);
                    setFieldError(false);
                    setPasswordError(false);

                }
                else if (e.response?.status === 400){
                    //Un champ ne convient pas
                    setEmailError(false);
                    setUpdateDone(false);
                    setFieldError(true);
                    setPasswordError(false);
                }

            })
            }

    function handleInputChange (event: React.ChangeEvent<HTMLInputElement>) {
            const { name, value } = event.target;
            setUpdatedUser({ ...updatedUser, [name]: value });
          }

    function handlePasswordChange (event: React.ChangeEvent<HTMLInputElement>) {
                const value = event.target.value;
                setUpdatedUser({ ...updatedUser, password: value });
                if(!isPasswordValid(value)){
                    setPasswordError(true);
                    setUpdateDone(false);
                    setEmailError(false);
                    setFieldError(false);
                }
                else{
                    setPasswordError(false);
                }
              }

    function getRandomPassword(){
        if (refPasswordInput.current){
            updatedUser.password = generatePassword();
            refPasswordInput.current.value = updatedUser.password
            setPasswordError(false);
        }

    }

    return (
        <main className="d-flex flex-column w-100 h-100 justify-content-between ">
            <div className="w-100 text-center bg-white">Modification de mon compte</div>
            {
                updateDone &&
            <div className="validation-card align-self-center">
                Compte mis à jour avec succès.
            </div>
            }
            {
                emailError &&
            <div className="error-card align-self-center">
                Erreur : cet email correspond à un autre compte.
            </div>
            }
            {
                fieldError &&
            <div className="error-card align-self-center">
                Erreur : un ou plusieurs champs ne conviennent pas.
            </div>
            }
            {
                passwordError &&
            <div className="error-card align-self-center">
                Le mot de passe doit contenir au moins 8 caractères, des majuscules, minuscules, chiffres et caracteres speciaux.
            </div>
            }
            <div className="form-card bg-white align-self-center d-flex flex-column overflow-auto">
                <form onSubmit={updateUser} >
                    <div className="mb-3">
                        <label htmlFor="firstName" className="form-label">Prénom</label>
                        <input type="text"  className="form-control" id="firstName"
                               name="firstName" placeholder="Saisir votre prénom" minLength={2} maxLength={30} defaultValue={user?.firstName} required
                               onChange={handleInputChange}/>
                    </div>
                    <div className="mb-3">
                        <label htmlFor="familyName" className="form-label">Nom</label>
                        <input type="text" className="form-control" id="familyName"
                               name="familyName" placeholder="Saisir votre nom" minLength={2} maxLength={30} defaultValue={user?.familyName}
                               onChange={handleInputChange}
                                required/>
                    </div>
                    <div className="mb-3">
                        <label htmlFor="email" className="form-label">Email</label>
                        <input type="email" className="form-control" id="email" name="email"
                               placeholder="Saisir votre email" minLength={5} maxLength={50} defaultValue={user?.email}
                                onChange={handleInputChange}
                                required/>
                    </div>
                    <div className="mb-3">
                        <label htmlFor="password" className="form-label">Mot de passe</label>
                        <input type="input" className="form-control" id="password"
                               name="password" placeholder="Saisir votre mot de passe" minLength={8} maxLength={50} defaultValue={user?.password}
                               onChange={handlePasswordChange}
                               ref={refPasswordInput}
                               required />
                        <a className="generate-link text-primary ms-1" onClick={getRandomPassword}>Générer un mot de passe</a>

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

export default MyAccount;
