<?php
                                    /* Connexion à la base de données*/

class Database {
    public static function connect() {
        $dsn = 'mysql:dbname=1613646_linkr;host=127.0.0.1';
        $user = 'root';
        $password = '';
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
   $query = "SELECT Last_Name, First_Name, State, Date_Request, Date_Accept, Date_Meeting, Subject FROM USER, (SELECT * FROM Meeting WHERE ID1=?) as c WHERE ID=ID1";
   $sth = $dbh->prepare($query);
   $sth->setFetchMode(PDO::FETCH_ASSOC);
   $sth->execute(array($ID));
   $result = $sth->fetchAll(PDO::FETCH_ASSOC);
   
   
   if (!empty($result)) {
        // check for empty result
        if (mysql_num_rows($result) > 0) {
 
            $result = mysql_fetch_array($result);
 
            $meeting = array();
            $meeting["Last_Name"] = $result["Last_Name"];
            $meeting["First_Name"] = $result["First_Name"];
            $meeting["State"] = $result["State"];
            $meeting["Date_Request"] = $result["Date_Request"];
            $meeting["Date_Accept"] = $result["Date_Accept"];
            $meeting["Date_Meeting"] = $result["Date_Meeting"];
            $meeting["Subject"] = $result["Subject"];
            // success
            $response["success"] = 1;
 
            // user node
            $response["meeting"] = array();
 
            array_push($response["meeting"], $meeting);
 
            // echoing JSON response
            echo json_encode($response);
        } else {
            // no product found
            $response["success"] = 0;
            $response["message"] = "No Meeting found";
 
            // echo no users JSON
            echo json_encode($response);
        }
    } else {
        // no product found
        $response["success"] = 0;
        $response["message"] = "No Meeting found";
 
        // echo no users JSON
        echo json_encode($response);
    }
   
}
?>