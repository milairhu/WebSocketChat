import axios from 'axios';

const api = axios.create({
    baseURL: `http://localhost:8080/`
})
// Intercepte le token et l'ajoute à chaque en-tête de requête
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        console.log("Intercepte token : " + token);
        config.headers.Authorization = "Bearer "+token; //Besoin d'ajouter le préfixe Bearer
    }
    return config;
}, error => {
    return Promise.reject(error);
});
export default api;