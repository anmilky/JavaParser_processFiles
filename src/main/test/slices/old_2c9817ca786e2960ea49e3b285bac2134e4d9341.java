MethodDeclaration
public PeerNode closerPeer ( PeerNode , HashSet , HashSet , double , boolean ) 
Parameters
PeerNode Var 1 
HashSet Var 2 
HashSet Var 3 
double Var 4 
boolean Var 5 
PeerNode Var 6  =  _ closer Peer ( Var 1 , Var 2 , Var 3 , Var 4 , Var 5 , false ) ; 
if 
Var 6  = = null & & Var 7 . get ( ) . is Advanced Darknet Enabled ( ) 
 0 
------------------------------
MethodDeclaration
public PeerNode closerPeer ( PeerNode , HashSet , HashSet , double , boolean ) 
Parameters
PeerNode Var 1 
HashSet Var 2 
HashSet Var 3 
double Var 4 
boolean Var 5 
PeerNode Var 6  =  _ closer Peer ( Var 1 , Var 2 , Var 3 , Var 4 , Var 5 , false ) ; 
if 
Var 6  ! = null & & Var 7 . get ( ) . is Advanced Darknet Enabled ( ) 
PeerNode Var 8  =  _ closer Peer ( Var 1 , Var 2 , Var 3 , Var 4 , Var 5 , true ) ; 
if 
Var 8  ! = null
Var 7 . missRoutingDistance . report ( distance ( Var 6 , Var 8 . get ( ) . get ( ) ) ) ; 
int Var 1 0  = Var 7 . get ( Var 9 . PEER _ NODE _ STATUS _ CONNECTED ) ; 
int Var 1 1  = Var 7 . get ( Var 9 . PEER _ NODE _ STATUS _ ROUTING _ BACKED _ OFF ) ; 
if 
Var 1 1  + Var 1 0  >  0 
Var 7 . backedoffPercent . report ( ( double ) Var 1 1  /  ( double )  ( Var 1 1  + Var 1 0 ) ) ; 
return Var 6 ; 
 0 
------------------------------
MethodDeclaration
public PeerNode closerPeer ( PeerNode , HashSet , HashSet , double , boolean ) 
Parameters
PeerNode Var 1 
HashSet Var 2 
HashSet Var 3 
double Var 4 
boolean Var 5 
PeerNode Var 6  =  _ closer Peer ( Var 1 , Var 2 , Var 3 , Var 4 , Var 5 , false ) ; 
if 
Var 6  ! = null & & Var 7 . get ( ) . is Advanced Darknet Enabled ( ) 
PeerNode Var 8  =  _ closer Peer ( Var 1 , Var 2 , Var 3 , Var 4 , Var 5 , true ) ; 
if 
Var 8  ! = null
Var 7 . missRoutingDistance . report ( distance ( Var 6 , Var 8 . get ( ) . get ( ) ) ) ; 
int Var 1 0  = Var 7 . get ( Var 9 . PEER _ NODE _ STATUS _ CONNECTED ) ; 
int Var 1 1  = Var 7 . get ( Var 9 . PEER _ NODE _ STATUS _ ROUTING _ BACKED _ OFF ) ; 
if 
Var 1 1  + Var 1 0  >  0 
return Var 6 ; 
 0 
------------------------------
MethodDeclaration
public PeerNode closerPeer ( PeerNode , HashSet , HashSet , double , boolean ) 
Parameters
PeerNode Var 1 
HashSet Var 2 
HashSet Var 3 
double Var 4 
boolean Var 5 
PeerNode Var 6  =  _ closer Peer ( Var 1 , Var 2 , Var 3 , Var 4 , Var 5 , false ) ; 
if 
Var 6  ! = null & & Var 7 . get ( ) . is Advanced Darknet Enabled ( ) 
 0 
------------------------------
MethodDeclaration
public PeerNode closerPeer ( PeerNode , HashSet , HashSet , double , boolean ) 
Parameters
PeerNode Var 1 
HashSet Var 2 
HashSet Var 3 
double Var 4 
boolean Var 5 
PeerNode Var 6  =  _ closer Peer ( Var 1 , Var 2 , Var 3 , Var 4 , Var 5 , false ) ; 
if 
Var 6  = = null & & Var 7 . get ( ) . is Advanced Darknet Enabled ( ) 
return Var 6 ; 
 0 
------------------------------
MethodDeclaration
public PeerNode closerPeer ( PeerNode , HashSet , HashSet , double , boolean ) 
Parameters
PeerNode Var 1 
HashSet Var 2 
HashSet Var 3 
double Var 4 
boolean Var 5 
PeerNode Var 6  =  _ closer Peer ( Var 1 , Var 2 , Var 3 , Var 4 , Var 5 , false ) ; 
if 
Var 6  ! = null & & Var 7 . get ( ) . is Advanced Darknet Enabled ( ) 
PeerNode Var 8  =  _ closer Peer ( Var 1 , Var 2 , Var 3 , Var 4 , Var 5 , true ) ; 
if 
Var 8  = = null
return Var 6 ; 
 0 
------------------------------
