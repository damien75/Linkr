<?php
                                    /* Connexion à la base de données*/

class Database {
    public static function connect() {
        $dsn = 'mysql:dbname=golinkrvv1;host=mysql51-113.perso';
        $user = 'golinkrvv1';
        $password = 's3y4ruZDxugK';
        $dbh = null;
        try {
            $dbh = new PDO($dsn, $user, $password,array(PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8"));
            $dbh->setAttribute( PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION );
        } catch (PDOException $e) {
            echo 'Connexion échouée : ' . $e->getMessage();
            exit(0);
        }
        return $dbh;
    }

}


                                    /*Gestion des utilisateurs dans la table: utilisateurs*/

class User {
    // Company	Exp_Years	Sum_Grade	Number_Grade
    public $ID;
    public $Last_Name;
    public $First_Name;
    public $Loc_X;
    public $Loc_Y;
    public $Last_Subject;
    public $Picture;
    public $Company;
    public $Exp_Years;
    public $Sum_Grade;
    public $Number_Grade;
}

    /*Imprimer les informations de tous les utilisateur*/
function getUsers() {
    $dbh = Database::connect();
    $query = "SELECT * FROM User";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array());
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode($result);
}
function getRequests($ID){
   $response = array();
   $dbh = Database::connect();
   $query = "SELECT Last_Name, First_Name, State, Date_Request, Date_Accept, Date_Meeting, Subject FROM user, (SELECT * FROM meeting WHERE ID1=?) as c WHERE ID=ID1";
   $sth = $dbh->prepare($query);
   $sth->setFetchMode(PDO::FETCH_ASSOC);
   $sth->execute(array($ID));
   $result = $sth->fetchAll(PDO::FETCH_ASSOC);
   
   
   if (!empty($result)) {
 
       $response["success"] = 1;
            $response["meeting"] = array();
            while($row = next($result)){
            $meeting = array();
            $meeting["Last_Name"] = $row["Last_Name"];
            $meeting["First_Name"] = $row["First_Name"];
            $meeting["State"] = $row["State"];
            $meeting["Date_Request"] = $row["Date_Request"];
            $meeting["Date_Accept"] = $row["Date_Accept"];
            $meeting["Date_Meeting"] = $row["Date_Meeting"];
            $meeting["Subject"] = $row["Subject"];
            array_push($response["meeting"], $meeting);
            }
            // echoing JSON response
            echo json_encode($response);
        }
     else {
        // no product found
        $response["success"] = 0;
        $response["message"] = "No Meeting found";
 
        // echo no users JSON
        echo json_encode($response);
    }
}
?>