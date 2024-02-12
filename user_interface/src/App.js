import Main from './component/Main'
import Login from "./component/login/Login";
import {useState} from "react";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import LoginForgotten from "./component/login/LoginForgotten";
import EditChat from "./component/EditChat";

function App() {



    return(
            <BrowserRouter>
                <Routes>
                    <Route exact path={"/"}  element={<Login />} />
                    <Route exact path={"/login"}  element={<Login />} />
                    <Route exact path={"/login/forgotten"}  element={<LoginForgotten />} />
                    <Route exact path={"/chats"}  element={<Main />} />
                    <Route exact path={"/chats/:chatId/edit"}  element={<EditChat />} />
                </Routes>
            </BrowserRouter>
        )



}

export default App;
