/**
 * Object describing a user
 * @export
 * @interface User
 */
export interface User {
    /**
     * Type id
     * @type {number}
     * @memberof User
     */
    id: number;
    /**
     * Type firstName
     * @type {string}
     * @memberof User
     */
    firstName: string;

    /**
     * Type familyName
     * @type {string}
     * @memberof User
     */
    familyName: string;

    /**
     * Type email
     * @type {string}
     * @memberof User
     */
    email: string;

    /**
     * Type password
     * @type {string}
     * @memberof User
     */
    password: string;

    /**
     * Type isAdmin
     * @type {boolean}
     * @memberof User
     */
    isAdmin: boolean;
    /**
     * Type isDeactivated
     * @type {boolean}
     * @memberof User
     */
    isDeactivated: boolean;
}
