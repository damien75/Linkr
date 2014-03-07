<?php

function connect() {
    $dsn = 'mysql:dbname=golinkrvv1;host=mysql51-113.perso';
    $user = 'golinkrvv1';
    $password = 's3y4ruZDxugK';
    $dbh = null;
    try {
        $dbh = new PDO($dsn, $user, $password, array(PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8"));
        $dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    } catch (PDOException $e) {
        echo 'Connexion Ã©chouÃ©e : ' . $e->getMessage();
        exit(0);
    }
    return $dbh;
}

function acceptProposition($IDm) {
    $dbh = connect();
    $query = "UPDATE meeting SET State=? , Date_Accept=? ,Time=default , Visibility=? WHERE IDm=?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array("1", date('Y-m-d H:i:s'), "1", $IDm));
    $response = array();
    $n = $sth->rowCount();
        if ($n == 1) {
            $response["success"]=TRUE;
        } else {
            $response["success"]=FALSE;
        }
        echo json_encode($response);
}

function addMessage ($ID1,$ID2,$message){
    $dbh = connect();
    $date = date('Y-m-d H:i:s');
    $query = "INSERT INTO chat (`ID1`, `ID2`, `Message`, `Date`, `Visibility`) VALUES (?,?,?,?,?)";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array($ID1, $ID2, $message,$date,"1"));
    $response = array();
    $n = $sth->rowCount();
    $l = $dbh -> lastInsertId();
        if ($n == 1) {
            $response["success"]=TRUE;
            $response["lastID"]=$l;
            $response["date"]=$date;
        } else {
            $response["success"]=FALSE;
        }
        echo json_encode($response);
}

function checkUpdateSentRequests($timestamp,$myID){
    $dbh = connect();
    $response = array();

    $query = "SELECT IDm,State,Subject, Message,Date_Accept,Date_Request,ID,Last_Name, First_Name FROM user, (SELECT * FROM meeting WHERE ID1=? AND (State=1 OR State=-1) AND Time>?) as c WHERE ID=ID2 ORDER BY Date_Accept DESC";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array($myID,$timestamp));
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    while ($row = current($result)) {
        array_push($response,$row);
        $row = next($result);
    }
    echo json_encode($response);
}

function createMeeting($ID1, $ID2, $Subject, $Message) {
    $dbh = connect();
    $response = array();

    $query = "INSERT INTO meeting (`ID1`, `ID2`, `State`, `Subject`, `Message`, `Date_Request`, `Date_Accept`,`Date_Meeting`,`Time`,`Visibility`) VALUES (?,?,'0',?,?,?,null,null,default,'2')";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    if ($sth->execute(array($ID1, $ID2, $Subject, $Message, date('Y-m-d H:i:s')))){
        $response["ID"] = $dbh -> lastInsertId();
        $response["success"] = 1;
    }
    else{
        $response["success"] = 0;
    }
    echo json_encode($response);
}

function createProfile($IDL, $Last_Name, $First_Name, $Company, $Exp_Years,$Picture) {
    $dbh = connect();
    $response = array();

    $query = "INSERT INTO user (`IDL`, `Last_Name`, `First_Name`, `Company`, `Exp_Years`, `Picture`) VALUES (?,?,?,?,?,?)";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    if ($sth->execute(array($IDL, $Last_Name, $First_Name, $Company, $Exp_Years, $Picture))) {
        $response["success"] = 1;
        $response["ID"] = $dbh -> lastInsertId() ;
    } else {
        $response["success"] = 0;
    }
    echo json_encode($response);
}

function existIDL($IDL) {
    $dbh = connect();
    $response = array();

    $query = "SELECT ID FROM user WHERE IDL=?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array($IDL));
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);
    if (!empty($result)) {
        $response["success"] = 1;
        $response["ID"] = current($result)["ID"];
        echo json_encode($response);
    } else {
        $response["success"] = 0;
        echo json_encode($response);
    }
}

define('THUMBNAIL_IMAGE_MAX_WIDTH', 150);
define('THUMBNAIL_IMAGE_MAX_HEIGHT', 150);

function generate_image_thumbnail($source_image_path, $thumbnail_image_path)
{
    list($source_image_width, $source_image_height, $source_image_type) = getimagesize($source_image_path);
    switch ($source_image_type) {
        case IMAGETYPE_GIF:
            $source_gd_image = imagecreatefromgif($source_image_path);
            break;
        case IMAGETYPE_JPEG:
            $source_gd_image = imagecreatefromjpeg($source_image_path);
            break;
        case IMAGETYPE_PNG:
            $source_gd_image = imagecreatefrompng($source_image_path);
            break;
    }
    if ($source_gd_image === false) {
        return false;
    }
    $source_aspect_ratio = $source_image_width / $source_image_height;
    $thumbnail_aspect_ratio = THUMBNAIL_IMAGE_MAX_WIDTH / THUMBNAIL_IMAGE_MAX_HEIGHT;
    if ($source_image_width <= THUMBNAIL_IMAGE_MAX_WIDTH && $source_image_height <= THUMBNAIL_IMAGE_MAX_HEIGHT) {
        $thumbnail_image_width = $source_image_width;
        $thumbnail_image_height = $source_image_height;
    } elseif ($thumbnail_aspect_ratio > $source_aspect_ratio) {
        $thumbnail_image_width = (int) (THUMBNAIL_IMAGE_MAX_HEIGHT * $source_aspect_ratio);
        $thumbnail_image_height = THUMBNAIL_IMAGE_MAX_HEIGHT;
    } else {
        $thumbnail_image_width = THUMBNAIL_IMAGE_MAX_WIDTH;
        $thumbnail_image_height = (int) (THUMBNAIL_IMAGE_MAX_WIDTH / $source_aspect_ratio);
    }
    $thumbnail_gd_image = imagecreatetruecolor($thumbnail_image_width, $thumbnail_image_height);
    imagecopyresampled($thumbnail_gd_image, $source_gd_image, 0, 0, 0, 0, $thumbnail_image_width, $thumbnail_image_height, $source_image_width, $source_image_height);
    imagejpeg($thumbnail_gd_image, $thumbnail_image_path, 90);
    imagedestroy($source_gd_image);
    imagedestroy($thumbnail_gd_image);
    return true;
}

/*
 * Uploaded file processing function
 */

define('UPLOADED_IMAGE_DESTINATION', './images/');
define('THUMBNAIL_IMAGE_DESTINATION', './thumbnails/');

function process_image_upload($id, $link)
{
    //$temp_image_path = $_FILES[$field]['tmp_name'];
    //$temp_image_name = $_FILES[$field]['name'];
    $temp_image_path = $link;
    $temp_image_name = $id;
    list(, , $temp_image_type) = getimagesize($temp_image_path);
    if ($temp_image_type === NULL) {
        return false;
    }
    switch ($temp_image_type) {
        case IMAGETYPE_GIF:
            break;
        case IMAGETYPE_JPEG:
            break;
        case IMAGETYPE_PNG:
            break;
        default:
            return false;
    }
    $uploaded_image_path = UPLOADED_IMAGE_DESTINATION . $temp_image_name;
    move_uploaded_file($temp_image_path, $uploaded_image_path);
    $thumbnail_image_path = THUMBNAIL_IMAGE_DESTINATION . preg_replace('{\\.[^\\.]+$}', '.jpg', $temp_image_name);
    $result = generate_image_thumbnail($uploaded_image_path, $thumbnail_image_path);
    return $result ? array($uploaded_image_path, $thumbnail_image_path) : false;
}

function getDebatingRequests($ID) {
    $dbh = connect();
    $response = array();
    $query = "SELECT IDm,Date_Request,Date_Accept, Subject,u1.ID as ID1,u1.Last_Name as Last_Name1, u1.First_Name as First_Name1,u2.ID as ID2,u2.Last_Name as Last_Name2, u2.First_Name as First_Name2 
FROM user as u1, user as u2, (SELECT * FROM meeting WHERE (ID2=? || ID1=?) AND State=?) as c
WHERE u1.ID=ID1 AND u2.ID=ID2";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);

    $sth->execute(array($ID,$ID, "1"));
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    while ($row = current($result)) {
        $meeting=array();
        
        $meeting["IDm"]=$row["IDm"];
        $meeting["Date_Request"]=$row["Date_Request"];
        $meeting["Date_Accept"]=$row["Date_Accept"];
        $meeting["Subject"]=$row["Subject"];
        if ($row["ID1"]==$ID){
            $meeting["ID"]=$row["ID2"];
            $meeting["First_Name"]=$row["First_Name2"];
            $meeting["Last_Name"]=$row["Last_Name2"];
            $meeting["MyStatus"]="1";
        }
        else{
            $meeting["ID"]=$row["ID1"];
            $meeting["First_Name"]=$row["First_Name1"];
            $meeting["Last_Name"]=$row["Last_Name1"];
            $meeting["MyStatus"]="2";
        }
        array_push($response, $meeting);
        $row = next($result);
    }
    echo json_encode($response);
}

function getLastMessage ($ID1,$ID2,$timestamp){
    $dbh = connect();
    $query = "SELECT IDmsg,Date,Message FROM chat WHERE Date>? AND ID1=? AND ID2=? ORDER BY Date";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array($timestamp,$ID1, $ID2));
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);
    $response = array();

    while ($row = current($result)) {
        array_push($response, $row);
        $row = next($result);
    }
        echo json_encode($response);
}

function getLastSubject($ID) {
    $response = array();
    $dbh = connect();
    $query = "SELECT Last_Subject FROM user WHERE ID=?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array($ID));
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    $response["subject"] = array();
    while ($row = current($result)) {
        $subject = array();
        $subject["Last_Subject"] = $row["Last_Subject"];
        array_push($response["subject"], $subject);
        $row = next($result);
    }
    echo json_encode($response);
}

function getLatestID() {
    $dbh = connect();
    $sth = $dbh->prepare("SELECT * FROM `user` ORDER BY `Time` DESC");
    $sth->execute();
    return $sth->fetch(PDO::FETCH_ASSOC)['ID'];
}

function getProfile($ID) {
    $dbh = connect();
    $response = array();

    $query = "SELECT Last_Name, First_Name, Loc_X, Loc_Y, Last_Subject, Company, Exp_Years, Sum_Grade, Number_Grade, Picture FROM user WHERE ID=?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);

    $sth->execute(array($ID));
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    while ($row = current($result)) {
        $response = $row;
        $row = next($result);
    }
    echo json_encode($response);
}

function getProfileIDs($XU, $YU, $E,$myID) {
    $IDMIN = intval($IDMIN);
    $NBDOWN = intval($NBDOWN);
    $XU = doubleval($XU);
    $YU = doubleval($YU);
    $E = doubleval($E);
    $dbh = connect();
    $response = array();

    $query = "SELECT ID FROM user WHERE Loc_X-?>-? AND Loc_X-?<? AND Loc_Y-?>-? AND Loc_Y-?<? AND ID<>?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array($XU, $E, $XU, $E, $YU, $E, $YU, $E,$myID));
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    while ($row = current($result)) {
        array_push($response, $row["ID"]);
        $row = next($result);
    }

    echo json_encode($response);
}

function getProfilesID2($IDInterdits,$XU, $YU, $E,$myID) {
    $IDMIN = intval($IDMIN);
    $NBDOWN = intval($NBDOWN);
    $XU = doubleval($XU);
    $YU = doubleval($YU);
    $E = doubleval($E);
    $dbh = connect();


    $dbh = connect();
    $response = array();
    if (count($IDInterdits) == 0){
    $query = "SELECT ID FROM user WHERE ID NOT IN (SELECT ID1 FROM meeting WHERE ID2=?) AND ID NOT IN (SELECT ID2 FROM meeting WHERE ID1=?) AND Loc_X-?>-? AND Loc_X-?<? AND Loc_Y-?>-? AND Loc_Y-?<? AND ID<>?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array($myID,$myID,$XU, $E, $XU, $E, $YU, $E, $YU, $E,$myID));
    }
    else{
    $query = "SELECT ID FROM user WHERE ID NOT IN (SELECT ID1 FROM meeting WHERE ID2=?) AND ID NOT IN (SELECT ID2 FROM meeting WHERE ID1=?) AND Loc_X-?>-? AND Loc_X-?<? AND Loc_Y-?>-? AND Loc_Y-?<? AND ID<>? AND ID NOT IN (" . str_repeat('?,', count($IDInterdits) - 1) . "?)";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    array_unshift($IDInterdits,$myID,$myID,$XU, $E, $XU, $E, $YU, $E, $YU, $E,$myID);
    $sth->execute($IDInterdits);
    }

    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    while ($row = current($result)) {
        array_push($response, $row["ID"]);
        $row = next($result);
    }

    echo json_encode($response);
}

function getProfilePicture($ID) {
    $dbh = connect();

    $query = "SELECT Picture FROM user WHERE ID = ?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);

    $sth->execute(array($ID));
    $result = $sth->fetch(PDO::FETCH_ASSOC);

    header("Content-type: image/jpeg");
    if ($result && $result["Picture"] != NULL) {
        echo file_get_contents($result["Picture"]);
    } else {
        echo file_get_contents("./images/fallback.jpg");
    }
}

function getProfilePicture2($ID) {
    $dbh = connect();

    $query = "SELECT Picture FROM user WHERE ID = ?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);

    $sth->execute(array($ID));
    $result = $sth->fetch(PDO::FETCH_ASSOC);
    
    header("Content-type: image/jpeg");
    if ($result && $result["Picture"] != NULL) {
        echo file_get_contents($result["Picture"]);
    } else {
        echo file_get_contents("./images/fallback.jpg");
    }
}

function getProfilesInRange($IDs) {
    if (count($IDs) == 0)
        return;

    $dbh = connect();
    $response = array();

    $query = "SELECT ID, Last_Name, First_Name, Loc_X, Loc_Y, Last_Subject, Company, Exp_Years, Sum_Grade, Number_Grade, Picture FROM user WHERE ID IN (" . str_repeat('?,', count($IDs) - 1) . "?)";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);

    $sth->execute($IDs);
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    while ($row = current($result)) {
        $response[$row["ID"]] = $row;
        $row = next($result);
    }

    echo json_encode($response);
}

function getReceivedRequests($ID2) {
    $dbh = connect();
    $response = array();
    $query = "SELECT ID,Last_Name, First_Name,IDm, Date_Request, Subject FROM user, (SELECT * FROM meeting WHERE ID2=? AND State=?) as c WHERE ID=ID1";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);

    $sth->execute(array($ID2, "0"));
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    while ($row = current($result)) {
        array_push($response, $row);
        $row = next($result);
    }
    echo json_encode($response);
}

function getSentRequests($ID1) {
    $dbh = connect();
    $response = array();
    $query = "SELECT ID,Last_Name, First_Name,IDm, Date_Request, Subject, Message FROM user, (SELECT * FROM meeting WHERE ID1=? AND State=?) as c WHERE ID=ID2";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);

    $sth->execute(array($ID1, "0"));
    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    while ($row = current($result)) {
        array_push($response, $row);
        $row = next($result);
    }
    echo json_encode($response);
}

function insertPicture($link, $id){
        $img = 'http://golinkr.net/images/'.$id;
        file_put_contents($img, file_get_contents($link));
}

function refuseProposition($IDm) {
    $dbh = connect();
    $query = "UPDATE meeting SET State=? , Date_Accept=? ,Time=default , Visibility=? WHERE IDm=?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array("-1", date('Y-m-d H:i:s'), "1", $IDm));
    $response = array();
    $n = $sth->rowCount();
        if ($n == 1) {
            $response["success"]=TRUE;
        } else {
            $response["success"]=FALSE;
        }
        echo json_encode($response);
}

function shareLocation($ID, $loc_x, $loc_y) {
    $dbh = connect();
    $query = "UPDATE user SET Loc_X=?,Loc_Y=? WHERE ID=?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array($loc_x, $loc_y, $ID));
    $response = array();
    $n = $sth->rowCount();
        if ($n == 1) {
            $response["success"]=1;
        } else {
            $response["success"]=0;
        }
        echo json_encode($response);
}

function submitSubject($ID, $subject) {
    $dbh = connect();
    $query = "UPDATE user SET Last_Subject=? WHERE ID=?";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array($subject, $ID));
}

function updateArchiveSubject($ID, $subject) {
    $dbh = connect();
    $query = "INSERT INTO archive_subject VALUES (?,?,default,default)";
    $sth = $dbh->prepare($query);
    $sth->setFetchMode(PDO::FETCH_ASSOC);
    $sth->execute(array($ID, $subject));
}

?>