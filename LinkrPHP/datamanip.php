<?php
function connect(){
        $dsn = 'mysql:dbname=golinkrvv1;host=mysql51-113.perso';
        $user = 'golinkrvv1';
        $password = 's3y4ruZDxugK';
        $dbh = null;
        try {
            $dbh = new PDO($dsn, $user, $password,array(PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8"));
            $dbh->setAttribute( PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION );
        } catch (PDOException $e) {
            echo 'Connexion Ã©chouÃ©e : ' . $e->getMessage();
            exit(0);
        }
      return $dbh;
}
    function submitSubject($ID,$subject){
        $dbh=connect();
        $query="UPDATE user SET Last_Subject=? WHERE ID=?";
        $sth = $dbh->prepare($query);
        $sth->setFetchMode(PDO::FETCH_ASSOC);
        $sth->execute(array($subject,$ID));
    }
    
    function updateArchiveSubject ($ID,$subject){
        $query="INSERT into archive_subject values (?,?,null,null)";
        $sth = $dbh->prepare($query);
        $sth->setFetchMode(PDO::FETCH_ASSOC);
        $sth->execute(array($ID,$subject));

    }
    
    
   function getRequest($ID1){
   $dbh=connect();
   $response = array();

   $query = "SELECT Last_Name, First_Name, State, Date_Request, Date_Accept, Date_Meeting, Subject FROM user, (SELECT * FROM meeting WHERE ID1=?) as c WHERE ID=ID2";
   $sth = $dbh->prepare($query);
   $sth->setFetchMode(PDO::FETCH_ASSOC);

   $sth->execute(array($ID1));
   $result = $sth->fetchAll(PDO::FETCH_ASSOC);
   
   
   if (!empty($result)) {
 
       $response["success"] = 1;
            $response["meeting"] = array();
            while($row = current($result)){
            $meeting = array();
            $meeting["Last_Name"] = $row["Last_Name"];
            $meeting["First_Name"] = $row["First_Name"];
            $meeting["State"] = $row["State"];
            $meeting["Date_Request"] = $row["Date_Request"];
            $meeting["Date_Accept"] = $row["Date_Accept"];
            $meeting["Date_Meeting"] = $row["Date_Meeting"];
            $meeting["Subject"] = $row["Subject"];
            array_push($response["meeting"], $meeting);
            $row=next($result);
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

function getProfile($ID){
   $dbh=connect();
   $response = array();

   $query = "SELECT * FROM user WHERE ID=?";
   $sth = $dbh->prepare($query);
   $sth->setFetchMode(PDO::FETCH_ASSOC);

   $sth->execute(array($ID));
   $result = $sth->fetchAll(PDO::FETCH_ASSOC);
   
   
   if (!empty($result)) {
 
       $response["success"] = 1;
            $response["Profile_Info"] = array();
            while($row = current($result)){
            $pro = array();
            $pro["Last_Name"] = $row["Last_Name"];
            $pro["First_Name"] = $row["First_Name"];
            $pro["Loc_X"] = $row["Loc_X"];
            $pro["Loc_Y"] = $row["Loc_Y"];
            $pro["Last_Subject"] = $row["Last_Subject"];
            $pro["Company"] = $row["Company"];
            $pro["Exp_Years"] = $row["Exp_Years"];
            $pro["Sum_Grade"] = $row["Sum_Grade"];
            $pro["Number_Grade"] = $row["Number_Grade"];
            array_push($response["Profile_Info"], $pro);
            $row=next($result);
            }
            // echoing JSON response
            echo json_encode($response);
        }
     else {
        // no product found
        $response["success"] = 0;
        $response["message"] = "No Profile found";
 
        // echo no users JSON
        echo json_encode($response);
    }
}

function getProfilePicture($ID){
   $dbh=connect();

   $query = "SELECT picture FROM picture WHERE ID=?";
   $sth = $dbh->prepare($query);
   $sth->setFetchMode(PDO::FETCH_ASSOC);

   $sth->execute(array($ID));
   return $sth->fetch(PDO::FETCH_ASSOC);

}

?>			