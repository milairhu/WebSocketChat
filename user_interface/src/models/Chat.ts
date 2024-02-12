/**
 * Object describing a chat
 * @export
 * @interface Chat
 */
export interface Chat {
    /**
     * Type id
     * @type {number}
     * @memberof Chat
     */
    id: number;
    /**
     * Type title
     * @type {string}
     * @memberof Chat
     */
    title: string;

    /**
     * Type description
     * @type {string}
     * @memberof Chat
     */
    description: string;

    /**
     * Type string
     * @type {Date}
     * @memberof Chat
     */
    date: string;

    /**
     * Type duration
     * @type {number}
     * @memberof Chat
     */
    duration: number;

    /**
     * Type ownerId
     * @type {number}
     * @memberof Chat
     */
    ownerId: number;
}
