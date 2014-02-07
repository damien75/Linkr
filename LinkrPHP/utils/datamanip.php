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


function getUsers() {
    $dbh = Database::connect();
    $query = "SELECT * FROM User";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array());
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode($result);
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



    /*Imprimer les informations d'un utilisateur*/
}

?>