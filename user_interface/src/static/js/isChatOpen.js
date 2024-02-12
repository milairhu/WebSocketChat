export function isChatOpen (chat) {
    //Test si le chat est actuellement ouvert ou non

    if (chat){
        const today = new Date();
        const chatStart = new Date(chat.date);
        const chatEnd = new Date(chat.date);
        chatEnd.setHours(chatEnd.getHours() + Math.floor(chat.duration / 60) );
        chatEnd.setMinutes(chatEnd.getMinutes() + chat.duration % 60);
        if (chatStart <= today && today <= chatEnd) {
            return true
        }
    }

    return false
}