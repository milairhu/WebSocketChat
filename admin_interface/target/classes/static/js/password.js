function valid()
{
    //liste caractère spéciaux
    const speciaux = /[`!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~]/;
    const numbers = /[0123456789]/;
    const lowercase =/[abcdefghijklmnopqrstuvwxyz]/;
    const uppercase =/[ABCDEFGHIJKLMNOPQRSTUVWXYZ]/;

    const name = document.getElementById("familyName");
    const firstname = document.getElementById("firstName");
    const password = document.getElementById("password").value;

    //Boolean to validate or not the form
    let isValid = true

    if(speciaux.test(name.value) || numbers.test(name.value))
    {
        alert("Erreur : Le nom contient des caractères spéciaux ou des chiffres");
        isValid  = false
    }

    if(speciaux.test(firstname.value) || numbers.test(firstname.value))
    {
        alert("Erreur : Le prénom contient des caractères spéciaux ou des chiffres");
        isValid  = false
    }

    if(!speciaux.test(password) || !numbers.test(password)
        || !lowercase.test(password) || !uppercase.test(password)){
        alert("Composition du mot de passe : majuscules, minuscules, chiffres, caracteres speciaux.");
        isValid  = false
    }

    if(password.length < 8 ){
        alert("Longueur du mot de passe : minimum 8 ");
        isValid  = false
    }

    return isValid
}

function generatePassword(){
    let password = document.getElementById("password");

    const chars = "abcdefghijklmnopqrstuvwxyz!@#$%^&*()-+<>ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    //Tests
    const speciaux = /[`!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~]/;
    const numbers = /[0123456789]/;
    const lowercase =/[abcdefghijklmnopqrstuvwxyz]/;
    const uppercase =/[ABCDEFGHIJKLMNOPQRSTUVWXYZ]/;
    let pass = "";

    do{
        pass="";
        for (let x = 0; x < 12 ; x++){
            const i = Math.floor (Math.random () * chars.length);
            pass += chars.charAt (i);
        }

    }while ((!speciaux.test(pass) || ! numbers.test(pass) ||
        !lowercase.test(pass) || !uppercase.test(pass)))
    password.value = pass;
}
