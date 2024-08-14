# Chat application

Due on 17/06/2023.

## Project Objective

Main project of S03 course at the UTC, consists of implementing a chat room website. The goal is to familiarize with various technologies used in web development.

The work was carried out during Lab sessions with the advice and help of Mr. Grégory Question. The fundamental principles involved are drawn from the course taught by Mr. Ahmed Louis.

## Getting started

The first step is to launch the admin interface to create users. Go to the *admin_interface* folder and launch the interface. Then, on **localhost:8080**, you can create, delete and modify users. A default admin account is *jb@test.com* with password *br03Test\**. Some other accounts are already created and editable with an admin account.

Then, go to *user_interface*, launch with *num run start* and connect to **localhost:3000**. The user should log in with the credentials created in the admin interface. The user can then create and participate to chats if they are available. The owner of a chat can invite other users to an own chat, as well as modifying the chat. Invited users can only participate in the chat when it is available.

If the users can't find their password, they can reset it by clicking on the "Forgot password" link on the login page. They will receive an email with their password.

Moreover, an account is deactivated after a certain amount of failed login attempts. The administrator can reactivated the account.

## Project Architecture

The web application designed for this assignment consists of two Spring and React projects interacting as illustrated in the figure below.

![Project Architecture](/conception/Architecture.PG)

The first project, a Spring project, plays a dual role.
It contains both the services and web pages related to the administrator interface and the web services for the React project.

The administrator interface  is the part that is responsible for managing users. It is based on the VC (Model-View-Controller) architecture and uses Spring controllers, PA and OR for data access, Thymelead, HTML, SS, S (Query, Bootstrap, etc.) for the view. The server manipulates the data via an H2 or PHPMyAdmin database, depending on the desired configuration.
The controllers called in the context of the administrator interface therefore return HTML pages to the client so that they can navigate in the application. Access to these controllers is subject to user authentication, who must necessarily be an administrator.
The Rest controllers called by the React application are subject to the presentation of a WT Token and return SON objects.
The features of the administrator application are the creation of users, the modification of their information, their deletion and their display in tables sortable by column headers and filterable by name, first name or user email address.

Regarding the second project, the Chat server, it is the part that is responsible for managing real-time chat messages, as well as managing the life cycle of chat rooms. It is built with React and uses the WebSocket protocol to receive and broadcast messages on the appropriate channel. The method of broadcasting messages is detailed in the **Design** section.
A user only has access to this application if they are not an administrator and can create chats, modify or delete their own and invite other users to participate in their lounges.
The use of a **WT token** provided at login allows secure consumption of the Spring project's web services.
Within the project, the components are coded on TX files, to ensure the integrity of the variable types and to facilitate the integration of HTML code.

Access to resources via access control violation is prevented in our 2 applications.
Whether by session or token verification, the user is necessarily redirected to the login page if they try to access a page while they do not have the right by URL.

## Design

While the general structure of the project was suggested by the assignment subject, the design phase was carried out freely.

The class diagram contained in the "design" folder summarizes our design choices and the links between our different classes.

![Class Diagram](/conception/conception.png)

The **User** class represents a user of the application, with the attributes required by the subject.
The *email* and *password* fields allow the user to log into the application.
The *Admin* field redirects the user to the administrator interface or to the chat application depending on its value.
The *isDeactivated* field is true if the user is deactivated. In this case, they can no longer use the application until they are unlocked by an administrator.
The User class also contains attributes for the user's first and last name, an automatically generated unique identifier as well as constructors, setters and letters.

The **Chat** class represents a chat room.
It contains a title, a description, a start date in LocalDateTime format, a duration in minutes and the owner user's identifier.
The *date* and *duration* fields allow to calculate if a discussion can take place or not in the room.
The *owner* field, the owner's identifier, designates the creator of the chat, who also has modification rights on the object.
The class also presents, like all the others, constructors, setters and letters.
The subject did not specify how the deletion of a chat should be managed if its validity date is in progress, so we decided to prohibit the deletion of chats in this case.

The **Invitation** class in the diagram, called **Chaser** in the project contains all the chat invitations to which users have been invited. This is reflected by the use of two attributes acting as a primary key: *userid* and *chat*.
It allows to list the users participating in a chat, to which the owner user must be added.

The **Attempt** class counts the number of erroneous connection attempts by users.
We chose to separate this class from the **User** class, considering that the latter could evolve in certain contexts without this counter of bad connections.
It contains the fields *userid* and *nbAttemps*. In the application, when *nbAttemps* reaches the value of 5, the user whose email address is entered in the login form becomes deactivated and can no longer connect.
This precaution prevents so-called *but force* attacks.

Finally, the **WebSocket** class appearing in the diagram is not part of the application's model per se, but allows communication between users in chat rooms.
It distributes the messages received by a session to other sessions corresponding to the same chat, but to different users.
The formatting of messages circulating in SON format allows to know who is the sender and to which chat they are addressing. It also allows to know when a user connects or disconnect from the chat.

It should be noted that for all this design part, no foreign key is actually used. For example, nothing in the structure of the models allows to determine that *owner* actually corresponds to an existing user. This is due to the fact that the use of such methods was not discussed with the teaching team. This is obviously a potential improvement.

Also, it was decided to handle composition relationships between objects.
This results in, for example, if a user is deleted by the administrator, their chat invitations are also deleted, as are the chats they own and their number of connection attempts. Similarly, if a chat is deleted, the associated invitations are also deleted.

## Interactions between technologies

The technologies used aim to participate in the implementation of the architecture described in the first part. This section details a little more precisely the arrangement between the different components of the project and their interactions.

The first component is the **Spring Boot** project for the implementation of the user interface and the Rest API for the chat application.
This project launches on *localhost:8080*. Particular attention has been paid to securing rights on this application. Thus, if the user is not connected (or if their session is no longer active), for any request they make by URL, they are redirected to the login page.
A session opens and the user accesses the administrator interface if the connection is verified, if they are indeed an administrator and if they are not deactivated.
If the user connects on *localhost:8080* but is not an administrator, then they are redirected to the React application on *localhost:3000*. They will have to authenticate there a second time for security.
Note that the administrator interface operates independently of the React project, as it manipulates the Spring VC architecture.

The second component of the assignment is the **React** project. It runs on port *3000* of *localhost* and allows access to the user interface.
If the user goes to *localhost:3000*, they must authenticate in the same way as for the Spring project. Here too, they are redirected to the Spring project if they are an administrator, or access the application if the connection is successful.
Unlike the Spring Boot project, this project is not at all autonomous. It consumes the **web services** offered by the Rest API of the Spring project. For this, it uses the Axis API, as the *api.js* file allows to see. As for the user interface, access to web services is controlled by the use of the WT token. When the user logs in, the token is created and integrates the headers of all HTTP requests called during navigation in the application.

The last central component of the chat application is the manipulation of **WebSockets**. They allow communication between users via a chat. Their use has been described in the Design section (sending messages by a user, distributed to other users of the same chat) but is a little more detailed here.
By accessing a discussion space, the client opens a WebSocket connection via the URL **/What/{chat}/{userid}**. If a WebSocket was already used (if we were previously on another chat room), then the connection is closed. This notably notified the other users of our disconnection. The client then opens a new one. The opening of a session notified of a new connection to the other users. Finally, entering a message in the text bar and pressing *Enter* sends this message to the server.
On the Spring Boot server side, a **singlet WebSocketServer** manages all the **WebSocket sessions** opened with clients. When a session is opened with a client, it stores it by attributing *chat* and *userid* attributes to correctly differentiate the sessions from each other. Each message received from a user is thus redistributed to the other users of the same chat. As our application notified clients of the arrivals and departures of users in a chat, we had to format the messages circulating from the server to the clients.
A **message from the server to the client** is therefore a SON containing the following fields:
- *rendered* to differentiate the sessions,
- *username* to signal the name of the sender of a message to the other participants,
- *message* which contains the content of the message,
- *status* which can be *Open*, *Close* or *Message*

Thus, the client can ensure the reception of messages coming from the server and distinguish how to treat them. A *Message* type message allows to display the user's message in the chat window, with the name of the sender via the *username* field. An *Open* message allows to display that the user *userid* has arrived. Finally, an *Close* message allows to display that the user has left.

## Eco-responsible analysis of the application

With the help of the **Green** plug-in of our browser, we were able to evaluate the environmental impact of our application.
The feedback is generally positive and for the different tests carried out, never a score was below 85 out of 100. This is beneficial not only for the environment, but also for our users' experience.

### Environmental impact of the administrator interface and chat application

The following screenshots show some feedback from Green on the administrator interface:

![Eco-responsible analysis with the Green plugin: admin menu](/conception/ecoIndexAdmin.png)

![Eco-responsible analysis with the Green plugin: second evaluation](/conception/ecoIndexAdmin2.png)

![Eco-responsible analysis with the Green plugin: user creation](/conception/ecoIndexAdminCreate.png)

![Eco-responsible analysis with the Green plugin: admin history](/conception/EcoIndex.NG)

As shown in the first two screenshots, multiple evaluations of the same page can lead to different results. We imagine this could be due to the use of cache, which would improve results after the first use.

Our readings on the user interface lead to results of the same order, although possibly a little better as evidenced by these few screenshots:

![Eco-responsible analysis with the Green plugin: chat menu](/conception/ecoIndexReact.png)

![Eco-responsible analysis with the Green plugin: account](/conception/ecoIndexReactAccount.png)

If the results displayed for our 2 applications are convincing, we have not averaged the results by pages (which, as we have seen, can vary from one test to another). With the aim of identifying our least eco-responsible pages/routes, we could have carried out this kind of average with several dozen trials per page to conclude on the performance of each one and, if necessary, improve the most lacking.

### Analysis of best practices and improvement paths

Green's analyses raise points for improvement in our coding practices that could enhance the eco-responsible performance of our 2 applications. The recurring feedback is as follows:
- cached data is not managed correctly;
- avoid HTTP requests leading to errors;
- it is preferable to declare SS and S in separate files rather than in the HTML code;
- the number of domains involved should be limited (localhost, ajax, jsdelivr, etc...);
- prefer HTTP2 to HTTP1

While our knowledge does not allow us to understand some of these tips (especially about cache management and HTTP2), some improvements are largely within our reach, such as separating SS and S into dedicated files, which we have already partially done.
Finally, we note that the positive feedback from Green is probably due to the very simple nature of the application (mainly CRU) and that the analysis was unlikely to result in very bad results. If we had to develop, for example, a video streaming application, it is likely that the results would have been much less satisfactory.

## Improvement paths

The project being of considerable size and introducing many concepts, some points could be improved. Here we list a few approximations that we would have corrected with more time.

- **The visuals** of our pages could be improved. While we did our best, it turns out that we do not have the skills to imagine layouts combining elegance and efficiency. Also, although the application strives not to use static sizing methods (rem rather than px, use of flexbox), it is not *responsive* per se.

- **Password security** is not managed correctly. Indeed, they are stored in clear in the database and transit as such. We could have only registered a fingerprint of the passwords. The techniques used were not covered in class, so we considered this security feature optional for the assignment.

- The AVA objects constituting our data model do not handle **foreign references**. Among other things, nothing guarantees that an *owner* of a chat actually corresponds to a user's id. Here too, this was not used because the method was not covered in class or in To.

- HTTP responses and requests always handle User or Chat objects in their entirety. This is not optimized. When possible, we could have **handled smaller objects, Does** containing only the necessary fields. This would have required implementing many classes on the Spring side as well as on the React side, implying a time investment to consider.

- We did not necessarily seek to code an **eco-responsible** application. Precautions could have been taken, but our spectrum of knowledge on the subject was too reduced during development. After analysis via the Green plugin, we identified our main gaps on the environmental impact of the application which, even if it presents positive results, can be improved. Our improvement paths on the subject have been mentioned previously.
