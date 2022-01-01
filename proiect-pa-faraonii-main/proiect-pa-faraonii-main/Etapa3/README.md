# proiect-pa-faraonii
proiect-pa-faraonii created by GitHub Classroom

##
- Nume proiect: Proiect PA
- Nume echipă: Faraonii
- Membri echipă: Bița Răzvan-Nicolae, Iancu George, Pană Ștefan, Prioteasa Cristi-Andrei
- Grupa: 322CC

##
- Makefile:
	- BUILD: 
		 - se compilează sursele java și se generează fișierele .class;
		 - apoi se creează un fișier bot.jar;
		 - pentru fisierul bot.jar folosim și MANIFEST.MF care indică clasa care conține metoda main (ceva similar cu entry-point cred);
	- CLEAN: 
	 	 - ștergerea fișierelor .class și .jar și fișierele de debug;
	- RUN:   
	 	 - rulează programul (apoi pentru a folosi engine-ul în Xboard folosim xboard -fcp "make run" -debug);

##
- Structura proiectului:
- Etapa 1:
	- Clasa MyScanner: Clasa pentru citire (am luat-o din indicațiile pentru testele practice);
	- Clasa Pieces:    Conține codificările pentru fiecare piesă/celulă goală printr-o literă (ex.: WHITE/BLACK_KING = 'k/K'). Am ales să codificăm EMPTY_TITLE 
			   cu '_';
	- Clasa Position:  Reține poziția (linie și coloană) pentru o piesă într-o matrice (codificarea tablei de șah). De asemenea, realizează codificarea poziției
			   din matrice în formatul dorit de XBoard (de ex. (1, 4) -> e2);
	- Clasa Player:    Reține pozițiile pieselor unui jucător, prin câte un ArrayList<Position> pentru fiecare tip de piesă exceptând regele (la regină am
		           considerat cazul în care se ajunge cu un pion la capăt și se pot face mai multe), și mai avem o funcție folosită pentru debug;		
	- Clasa Game:      Reține stare jocului: o variabilă care zice dacă este rândul engine-ului sau al adversarului, o variabilă care zice ce culoare trebuie să
		           mute acum (0 pentru alb și 1 pentru negru), o variabilă de tip boolean care zice dacă engine-ul este pornit sau nu (pentru force și go), 
		           o matrice de caractere (codificarea tablei de șah), un vector Player[] players, cu players[0] -> jucătorul alb, players[1] -> negru. În 
		           această clasă, inițializăm jocul (tabla de joc, fiecare jucător, și toate piesele de pe tablă). De asemenea, avem o metodă prin care schimbăm
		           culoarea și comutăm între engine și oponent dacă nu suntem în force;
	- Clasa Xboard:    Aceasta este clasa în care comunicăm cu xboard (primim și scriem comenzi). La comanda "feature" am pus și opțiunile "usermove = 1" (ca să primim
	                   mutări sub forma "usermove e2e4") și variants=\"3check\" (pentru că asta e versiunea de șah);

- Etapa 2:
	- Clasa Move: 	   Retine pentru o mutare poziția inițială și pe cea finală a piesei mutate;
	- Clasa PromotionMove: Extinde clasa Move, având în plus un membru de tip caracter, care reprezintă piesa în care se transformă pionul în urma promovării;
	- Clasa State: 	   Retine datele care reprezintă o stare a jocului (cele care trebuie comparate atunci când comparăm stările pentru a vedea repetiții):
			   matricea corespunzătoare, culoarea care trebuie să mute, dacă este valid En-Passant, dacă nu au fost deja mutate turnurile corespunzătoare
			   rocadelor, respectiv regii.

- Etapa 3:
	- Clasa CharMatrix: 	     Wrapper peste o matrice care reprezintă tabla de joc. Folosită drept cheie într-un HashMap (caching);
	- Clasa KingSideCastleMove:  Clasă care extinde clasa Move, folosită pentru rocadă;
	- Clasa QueenSideCastleMove: Clasă care extinde clasa Move, folosită pentru rocadă;
	- Clasa MinimaxState: 	     Clasa care reține o stare din arborele Minimax (scor, joc, mutarea care a dus la această stare a jocului);

##
- Abordarea algoritmică a etapei:
- Etapa 1:
	- În general, pentru a realiza o mutare, știm poziția inițială și cea finală a piesei pe care o mutăm. Dacă unde mutăm este o piesă
	  a celuilalt jucător, vedem în matrice ce piesă este și o ștergem din ArrayList-ul piesei corespunzătoare a jucătorului (pentru etapa asta punem null,
	  pentru că în cazul pionului să știm că îl mutăm pe același). Apoi schimbăm poziția piesei (mutate) din ArrayList-ul corespunzător al jucătorului care
	  mută și actualizăm în matrice. De asemenea, dacă este vorba de o mutare de pion în urma căreia rezultă transformarea în regină, poziția pionului devine
          null și adăugăm o nouă regină în ArrayList-ul corespunzător;
	- Pentru mutările oponentului (primite de la xboard), decodificăm mutarea (format "e2e4") în coordonate din matricea 8x8. Verificăm dacă este
          o rocadă, iar în cazul în care este o rocadă, mutarea este spartă în 2 mutări (cea a regelui și cea a turei). Astfel, pentru fiecare codificare
	  de rocadă (care reprezintă mutarea regelui), asociem o mutare complementară (cea a turei). În rest, procesul de mutare este explicat mai sus;
	- Pentru mutările engine-ului, folosim o funcție care mută un pion cu index dat. Pentru început, ia din ArrayList-ul de pioni al culorii curente
	  poziția acestui pion. Dacă poziția este null, înseamnă că pionul a fost capturat -> întoarcem "resign". Altfel, pentru această poziție, se
          generează pozițiile în care merge mutat pionul (mai întâi se verifică dacă se poate captura - mers în diagonală, apoi dacă poate înainta cu 2 
	  celule, respectiv cu o celulă). Dacă nu s-a generat nicio astfel de poziție, nu mai putem muta pionul -> întoarcem "resign". Altfel, facem
	  mutarea (în matrice și ArrayList-uri) și construim șirul de forma "move e2e4" care va fi returnat (cu atenție în cazul în care este vorba
	  de o mutare în urma căreia rezultă transformarea pionului în regină, de pus un 'q' la finalul șirului).
	  
- Etapa 2:
	- Pentru a implementa mutările celorlalte piese am plecat de la ce am implementat la etapa 1 pentru pion;
	- Când engine-ul trebuie să mute, acesta verifică mai întâi dacă poate să facă o rocadă. În cazul în care poate, o face. Altfel, generează toate mutările
	  posibile pentru piesele sale și alege aleator una;
	- Pentru a genera toate mutările valide ale pieselor a fost nevoie să implementăm o funcție care verifică dacă o mutare este validă (isValidMove). Aceasta
	  verifică dacă în urma mutării, regele jucătorului care mută este sau nu în șah. Această funcție apelează funcția notInCheck care primește ca argument
	  poziția regelui și matricea corespunzătoare tablei și returnează True dacă nu este în șah, respectiv False dacă este în șah;
	- Am făcut câte o funcție care generează mutările valide pentru fiecare tip de piesă;
	- Bot-ul nu are cum să facă mutări invalide deoarece el ia în considerare o mutare doar dacă în urma ei, acesta nu se află în șah;
	- Pentru promovări: în plus față de etapa precedentă, pionii pot fi promovați și în Nebun, Cal, respectiv Turn, nu doar în Regină. De aceea, pentru mutările
	  de tip Promovare am folosit o clasă care reține printr-un caracter și tipul piesei care o să înlocuiască pionul (de asemenea, am modificat și la mutările
	  făcute de adversar);
	- Pentru En-Passant: fiecărui Player i-am asociat un membru de tip boolean canBeEnPassant, care devine True dacă ultima mutare a jucătorului este una cu pionul
	  cu două spații înainte și False dacă ultima mutare a jucătorului este de alt tip. De asemenea, am pus un membru Position lastPieceMoved care reține poziția pe
	  care a fost mutat acest pion în cazul mutării cu 2 spații înainte. Când engine-ul generează mutările Pionilor, verifică dacă ultima mutare a adversarului este
	  una cu pionul cu două spații înainte și verifică dacă un pion de al său îl poate captura prin En-Passant. De asemenea, în plus față de etapa 1, am luat în
	  considerare și dacă adversarul realizează o mutare de tip En-Passant;
	- Pentru Rocade: fiecărui Player i-am asociat doi membri de tip boolean queenSideCastleAvailable, respectiv kingSideCastleAvailable, care au valoarea False din
	  momentul în care regele a fost mutat sau tura corespunzătoare rocadei respective a fost mutată. Dacă queenSideCastleAvailable este True, sunt verificate celelalte
	  condițiii pentru rocadă (dacă spațiile dintre rege și turn sunt libere, dacă regele nu se află în șah, dacă regele nu trece prin șah, respectiv dacă nu ajunge în
	  șah). Similar pentru kingSideCastleAvailable. Pentru a realiza mutarea de tip Rocadă am folosit ideea de la prima etapă: se sparge în două mutări (a regelui și
	  a turnului). Nu am mai avut de implementat rocada pentru mutările adversarului, deoarece o implementasem la etapa 1;
	- Pentru condițiile de terminare a partidei: se verifică la finalul turei unei culori dacă:
		- se repetă de 3 ori aceeași stare a jocului (pentru asta avem o listă ArrayList<State> history în clasa Game. Atunci când eliminăm o piesă, golim întregul
		  istoric, deoarece stările dinainte nu se mai pot repeta. Astfel, acest ArrayList nu o să conțină prea multe elemente, având și limita de 50 de mutări
		  fără captură și fără mutări de pioni;
		- se verifică dacă am ajuns la 50 de mutări fără captură și fără mutări de pioni (avem un membru int nrMovesForDraw care reține numărul de mutări, pe care 
		  îl incrementăm la fiecare mutare și îl resetăm când mutăm pion sau când eliminăm o piesă);
		- pentru pat: se verifică dacă nu suntem în șah, dar nu mai putem muta;
		- pentru mat: se verifică dacă suntem în șah si nu mai putem muta;
		- 3 șahuri date unui rege: fiecare Player are un membru care reține de câte ori și-a luat șah. Dacă această valoare ajunge la 3, meciul se termină;
		- pentru terminarea partidelor, botul trimite la XBoard un mesaj de forma "scor {Observație}", unde scor poate fi: 1-0, 0-1, 1/2-1/2 și observație spune
		  cum se termină partida;

- Etapa 3:
	- am implementat un algoritm de tip Minimax cu Alpha-Beta pruning, cu adancime 4 sau 5 în funcție de momentul în care ne aflăm în joc;
	- am implementat o funcție care evaluează o stare a jocului (scorul acesteia), ținând cont de mai multe aspecte:
		- piesele de pe tabla de joc (valoarea lor);
		- numărul de mutări valide pentru fiecare tip de piesă;
		- pozițiile cailor (pe ce frontieră a tablei se află, dinspre exterior spre interior);
		- diverse tipuri de pioni (dublați, izolați, blocați, centrali);
		- dacă caii sunt în "outposts";
		- pozițiile turelor: liniile pe care se află, tipurile de pioni care sunt pe coloana cu tura, daca turele sunt conectate;
		- dacă jucătorul a făcut deja rocada;
		- numărul de nebuni (bonus pentru 2 sau mai mulți nebuni);
		- în funcție de numărul de șahuri date de fiecare jucător;
	- am folosit un HashMap pentru a reține scorurile stărilor deja evaluate;
	- am renunțat la funcțiile de debug;
##
- Surse de inspirație:
	- YouTube pentru a înțelege mai bine cum funcționează XBoard;
	- am căutat pe net pentru partea de compilare, fiindcă suntem obișnuiți să facă IDE-ul build;
	- clasa MyScanner din indicațiile pentru testele practice (https://pastebin.com/XGUjEyMN).

##
- Responsabilitatea fiecărui membru al echipei:
- Etapa 1:
	- Bița Răzvan-Nicolae: discuția despre cum implementăm, codificarea pieselor (clasa Pieces), deletePiece, moveAsPlayer, movePawn, README;
	- Iancu George: discuția despre cum implementăm, Makefile, changePosition, fișier debug, moveAsPlayer, movePawn, comentarii + aranjat cod, README;
	- Pană Ștefan: discuția despre cum implementăm, inițializare Game, deletePiece, changePosition, moveAsPlayer, movePawn, README;
	- Prioteasa Cristi-Andrei: discuția despre cum implementăm, clasa Position + ideea cu ArrayList din Player, moveAsPlayer, movePawn, comentarii + aranjat cod, README.

- Etapa 2:
	- Bița Răzvan-Nicolae: generarea mutărilor pentru Nebun, rocade, README, repetiție de 3 ori a aceeași poziție;
	- Iancu George: funcția care verifică situația de șah, generaraea mutărilor pentru Pion (+EnPassant +Promovări), README, repetiție de 3 ori a aceeași poziție;
	- Pană Ștefan: generarea mutărilor pentru Cal și pentru Rege, rocade, mat, pat, condiție 3 șahuri;
	- Prioteasa Cristi-Andrei: generarea mutărilor pentru Tură, condiție 50 de mutări, condiție 3 șahuri.

- Etapa 3:
	- Bița Răzvan-Nicolae: funcția de evaluare;
	- Iancu George: algoritmul Minimax, funcția de evaluare;
	- Pană Ștefan: funcția de evaluare;
	- Prioteasa Cristi-Andrei: funcția de evaluare.
