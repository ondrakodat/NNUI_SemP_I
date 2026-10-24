První semestrální práce NNUI1  
Bod A) 
Mějme agenta v 2D prostoru, jehož atributem je směr natočení (Sever, Jih, Východ, Západ). Agent 
může podniknout následující akce:  
• Otoč se o 90° doleva (cena 2 body)  
• Otoč se o 90° doprava (cena 2 body)  
• Otoč se o 180° (cena 3 body)  
• Udělej krok vpřed o jedno pole (cena 5 bodů)  
Černá barva vždy bude znamenat stěnu a bílá barva cestu. 
Navrhněte samostatně v libovolném programovacím jazyku implementaci A* algoritmu tak, aby 
agent (nepředstavovat jako člověka, ale v duchu principu algoritmů probíraných v předmětu) 
dorazil ze svého počátečního stavu do stavu koncového. Počáteční i koncový stav jsou volitelné. 
Přípustné akce jsou pouze ty, které agenta umístí na bílé pole.  
Počáteční orientace agenta je volitelná. 
Zobrazené bludiště v aplikaci musí být dobře čitelné jako na obrázkách níže. 
Přiložený soubor (Bludiste1.bmp) použijte jako testovací tvar bludiště. 
Výsledná aplikace zobrazí cenu celkové cesty a výslednou cestu (jak je zobrazeno na příkladu). 
Při spuštění ve vybraném prostředí (PyCharm, … ) se budou vypisovat vybraným způsobem 
souřadnice jaké jsou stavy prohledávány (tyto údaje se nebudou vypisovat při spuštění souboru 
.exe). 
Požadované věci na odevzdání: Kompletní zdrojové kódy aplikace, soubor .exe (spustitelný bez 
nutnosti instalace externích knihoven; pokud aplikace externí knihovny používá, musí být 
přiloženy tak, aby uživatel nemusel samostatně řešit jejich instalaci ani problém s chybějícími 
knihovnami), který spustí aplikaci a umožní uživateli zadat pouze počáteční a cílový stav (není 
potřeba při ukázání aplikace ve cvičení). V e-mailu krátce popsat návod na spuštění a kde se 
vypisují souřadnice v daném vybraném prostředí. 
Příklad bludiště: 
Červená – start/výsledná cesta 
Modrá – prohledaný prostor 

Bod B) 
Vypracujte zprávu. Zpráva bude mít následující povinné součásti:  
Dokument bude obsahovat všechny potřebné identifikační a formální náležitosti, zejména název 
univerzity a fakulty, jméno a příjmení studenta, studijní program, ročník studia, datum vytvoření a 
případně další relevantní údaje. Součástí dokumentu bude také přehledná struktura s nadpisy a 
podnadpisy a automaticky vytvořený obsah (osnova), aby byl dokument přehledný. 
Report o řešení včetně stručného popisu architektury programu, postupu řešení a diskuse o 
vhodnosti zvoleného řešení (např. diskuse volby heuristické funkce). Úvaha o jiných algoritmech 
probíraných na přednáškách předmětu NNUI1 (alespoň 3). 
Alespoň 5 obrázků s různým zadáním cíle a startu i s možností nenalezení cesty. U obrázků budou 
napsány výsledné ceny cesty. 
Spustitelná aplikace s kompletním návodem, zhruba jak aplikaci oživit na počítači s Windows 11 
Bod C) 
Bod C navazuje na bod A a B. 
Při programování algoritmů a částích s detekováním nepoužívejte umělou inteligenci ke 
generování kódu. V ostatních případech je použití umělé inteligence naopak doporučeno. 
Rozšiřte aplikaci o algoritmus BIDI, jež zohledňuje cenu cesty. 
V aplikaci bude combo box, v kterém se zobrazí všechny obrázky png, jpg a bmp (přiložené u 
scriptu/.exe) z kterých uživatel jeden vybere který se použije pro testování algoritmu. Uživatel bude 
moci opakovaně zadávat počáteční a cílový stav a případně změnit použité bludiště. V aplikaci 
bude možné volit jeden ze zadaných algoritmů. 
V aplikaci se dále zobrazí výsledný efektivní faktor větvení.  
Dále aplikace bude mít možnost zadání doby kroku tedy že se nové kroky budou objevovat 
postupně danou zadanou rychlostí (povinné zadatelné rozmezí je 10 – 1000ms). 
Také aplikace bude mít tlačítko pro zastavení a znovu spuštění algoritmu. 
Dále upravte aplikaci tak aby byla schopna identifikovat, zda se na vstupním obrázku jedná o 
stěnu či cestu (identifikace barvy).  
Aplikace musí být schopna rozpoznat bludiště tedy aby okolní věci okolo bludiště byli ignorovány. 
Jako na obrázku níže: 
V aplikaci se vypíše, co je cesta a co stěna. 
Za předpokladu více barevné stěny aplikace musí poznat cestu, pokud je cesta v podobném 
barevném rozsahu a stejně tak vše ostatní co je v rozdílném barevném rozsahu je bráno za stěnu.  
Dále aby algoritmus byl schopen zpracovat i jiné obrázky .png a .jpg. Dále Aplikace musí být 
schopna potlačit drobné nepřesnosti v bludišti, aby nedocházelo k chybování algoritmu. 
Předpoklad je stejná šířka cesty tedy aby agent měl rozměry čtverce… (v bludišti může být 
reprezentován tečkou či jinak) V případě drobné chyby šířky program musí umět potlačit tuto 
chybu (ignorací tohoto stavu) 
Zabraňte tomu, aby agent vyběhl z bludiště.  
Úprava vstupních obrázků s bludišti je zakázána jako změna barev stěn a cest a ořezávání obrázku, 
… s výjimkou zoom. 
Bod C přidejte do bodu B kde bude navíc: 
V protokolu uveďte, proč a jak jste zvolili způsob na rozpoznání cest a stěn. Také proč a jakou 
toleranci spektra jste zvolili. 
Pět experimentů pro A* a UCS (doložené fotkami) pro stejný cíl a start (zvolené vaší náhodnou 
valbou ale tak aby umístění bylo smysluplné pro provedení experimentu) s vypsanými vhodnými 
informacemi pro porovnání obou algoritmů. 
Poté bude obsahovat dalších pět experimentů pro každé přiložené bludiště ale už pouze pro jeden 
algoritmus tedy BIDI nebo A*. 
