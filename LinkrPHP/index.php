<?php
header('Content-type: application/json');
        $dsn = 'mysql:dbname=golinkrvv1;host=mysql51-113.perso';
        $user = 'golinkrvv1';
        $password = 's3y4ruZDxugK';
        $dbh = null;
        try {
            $dbh = new PDO($dsn, $user, $password,array(PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8"));
            $dbh->setAttribute( PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION );
           // echo "doudss";
        } catch (PDOException $e) {
            echo 'Connexion échouée : ' . $e->getMessage();
            exit(0);
        }
    $query = "SELECT `First_Name` FROM `user` WHERE ID1=1";
    $sth = $dbh->prepare($query);    
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    
    $sth->execute(array());
    echo "skj"; 
    $result = $sth->fetch(PDO::FETCH_ASSOC);
    echo $result;
    

?>