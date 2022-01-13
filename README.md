# Instant Messenger (Meshenger)
#### Projektmotivation
Das Projekt wird als Prüfungsleistung im Modul Verteilte Systeme des Studiengangs Kommunikationsinformatik an der HTW Saar durchgeführt.

#### Projektziel
Das Ziel des Projektes ist die Entwicklung and Realisierung eines Instant-Messenger Dienstes zum Austausch von Textnachrichten. Hierbei soll der Dienst sowohl Nutzer-zu-Nutzer Kommunikation, als auch Gruppenkommunikation ermöglichen. Zur Verwaltung von Nachrichten soll ein zentraler Server eingesetzt werden. Weiterhin soll sichergestellt sein, dass Nachrichten auch unabhängig vom Anmeldestatus eines Nutzers verfügbar sind, bzw. ausgetauscht werden können. Des Weiteren wurden von der Gruppe eigene Anforderungen an verschiedene Funktionalitäten, sowie an das Design gestellt. Diese orientierten sich meist an weit verbreiteten Messenger, wie bspw. WhatsApp.

#### Projektbeschreibung
Das Projekt soll in der Programmiersprache Java geschrieben werden, da die meisten Mitglieder dort bereits Kenntnisse hatten, bzw. während diesem Semester erlangen konnten. Weiterhin sollte der Dienst über eine Webapplikation verfügbar gemacht werden, sodass keine Abhängigkeit bei Wahl der Plattform besteht.

## Architektur
#### Use Cases / User Stories

![](https://cdn.discordapp.com/attachments/889182169732374579/889498738727477308/unknown.png)


#### Anforderungen
Darstellung der Anfoderungen unterteilt nach funktionalen und nichtfunktionalen Anforderungen sowie nach Must-, Should-, und Could-Have Anfoderungen

###### Funktionale Anforderungen
| Must | Should  | Could |
|--|--|--|
| Direktnachrichten (Einzel- und Gruppenchat | Plattformunabhängig |Avatarupload  |
| Zentraler Server zur Kommunikation | Webbasierte Anwendung | |
| Nachrichtenzuerstellung unabhängig von Online-Status | | |
| Stabile Serververbindung | | |
| Zentrale Datenbank| | |

###### Nichtfunktionale Anforderungen
| Must | Should  | Could |
|--|--|--|
| Modernes Design | New-Message Badge | JavaFX-Client Anwendung |
| Personalisierungsoptionen | Emoji's & Bilder |  |
| Easy to Use  |  |  |

#### Lösungsstrategie
Unsere Lösung basierte natürlich zunächst auf den uns gegebenen Vorgaben. Wir haben uns schnell auf die Programmiersprache Java geeignet, da diese im laufenden Semester Teil unseres Studiums war. Uns war es wichtig, ein plattformunabhängiges System zu realisieren, sodass der Zugriff egal von welchem Gerät möglich ist. Somit lag die Entscheidung einen Webserver aufzusetzen sehr nahe. Aufgrund von bereits existierenden Kenntnissen eines Teammitgliedes, haben wir uns für Tomcat in Verbindung mit Javascript entschieden. Die Daten sollten in einer MySQL Datenbank hinterlegt werden. Die Datenbank wird über - in Java programmierten Controllern - angesprochen. Die Verbindung zwischen Frontend (HTML und CSS) und den Controller wurde über Servlets realisiert, entsprechend dem Kapselungsprinzips. Der Webserver wird auf einem Raspberry Pi gehostet. Der Client wiederum ist losgelöst und kommuniziert mit dem Server über HTTP-Requests.
#### Statisches Modell

###### Bausteinsicht

![](https://cdn.discordapp.com/attachments/889182169732374579/889855197868879912/BausteinsichtNeu.png)

###### Verteilungssicht
![](https://cdn.discordapp.com/attachments/889182169732374579/889503005970399242/Verteilungssicht.png)

###### Klassendiagramme

Controller
![](https://cdn.discordapp.com/attachments/889182169732374579/889876361584472114/Controller.PNG)Models
![](https://cdn.discordapp.com/attachments/889182169732374579/889876376625238086/Models.PNG)
Servlets
![](https://cdn.discordapp.com/attachments/889182169732374579/889876378349076480/Servlets.PNG)
###### API
Chatrooms
![](https://cdn.discordapp.com/attachments/889182169732374579/889903844929192026/API_Chatrooms.PNG)-------------
Contacts
![](https://cdn.discordapp.com/attachments/889182169732374579/889903846153924628/API_Contacts.PNG)-------------
Login
![](https://cdn.discordapp.com/attachments/889182169732374579/889903847412211782/API_Login.PNG)-------------
Members
![](https://cdn.discordapp.com/attachments/889182169732374579/889903848959934495/API_Members.PNG)-------------
Messages
![](https://cdn.discordapp.com/attachments/889182169732374579/889903850297901146/API_Messages.PNG)-------------
Users
![](https://cdn.discordapp.com/attachments/889182169732374579/889903851933691974/API_Users.PNG)
#### Dynamisches Modell
Nachricht empfangen
![](https://cdn.discordapp.com/attachments/889182169732374579/889899272135397387/SQD_Nachricht_empfangen.png)-------------
Nachricht senden
![](https://cdn.discordapp.com/attachments/889182169732374579/889899648679026688/SQD_Nachricht_senden.png)-------------
Kontakt erstellen
![](https://cdn.discordapp.com/attachments/889182169732374579/889900136594026566/SQD_Kontakt_erstellen.png)-------------
Chatroom erstellen
![](https://cdn.discordapp.com/attachments/889182169732374579/889900377695203378/Chatroom_erstellen.png)

## Getting Started
Als Erstes wird die MySQL Datenbank installiert. Danach wird das Create-Skript ausgeführt, um die Datenbank zu füllen. In der Database.java sind die Datenbankinformationen sowie die Ressourcenordner anzupassen. Darauffolgend wird das Maven-Package erstellt und die daraus entstandene .war-Datei auf den Tomcat Webserver hochgeladen.

#### Vorraussetzungen
* Hostrechner (Windows, Linux, ...)
* Entwicklungsumgebung
* MySQL (Datenbank, Create-Skript ausführen)
* Maven (Packaging)
* Apache Tomcat (Webserver)

#### Installation und Deployment
1. Installieren der mySql Datenbank auf dem Host (https://dev.mysql.com/doc/mysql-installation-excerpt/5.7/en/)
2. Installieren des Tomcat-Servers (https://tomcat.apache.org/tomcat-8.5-doc/setup.html)
3. Herunterladen der Source-Dateien
4. Anpassen der Verbindungsinformationen in der Database.java
    1. URL zum Server
    2. Username
    3. Password
    4. Speicherorte der Avatare und Bilder
5. Anpassen der Domain in javascript (Zeile 16/17) (meist localhost)
5. Installieren von Maven (bspw. Plug-In für VSCode)
6. Projekt packagen mit dem Maven Plugin
7. Laden der entstandenen ".war" Datei auf den Tomcat-Server
8. Los Chatten!


## Built With
Geben Sie an, welche Frameworks und Tools Sie verwendet haben. Z.B.:
* [Visual Studio Code](https://code.visualstudio.com/download)
* [Apache Tomcat](http://tomcat.apache.org/)
* [Raspberry Pi](https://www.raspberrypi.org/)
* [MySQL](https://www.mysql.com/de/)
* [Bootstrap](https://getbootstrap.com/)
* [jQuery](https://jquery.com/)
* [GSON](https://github.com/google/gson)
* [Maven](https://maven.apache.org/)
* [FasterXML](https://github.com/FasterXML/jackson-core)
* [Servlet](https://jakarta.ee/specifications/servlet/)
* [Visual Paradigm](https://www.visual-paradigm.com/)

## License

This project is licensed under the GNU General Public License v3.0

