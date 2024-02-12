export function isPasswordValid(password)
{
    //liste caractère spéciaux
    const speciaux = /[`!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~]/;
    const numbers = /[0123456789]/;
    const lowercase =/[abcdefghijklmnopqrstuvwxyz]/;
    const uppercase =/[ABCDEFGHIJKLMNOPQRSTUVWXYZ]/;

    //Boolean to validate or not the form
    let isValid = true

    if(!speciaux.test(password) || !numbers.test(password)
        || !lowercase.test(password) || !uppercase.test(password)){
        isValid  = false
    }

    if(password.length < 8 ){
        isValid  = false
    }

    return isValid
}

export function generatePassword(){

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
    return pass;
}
