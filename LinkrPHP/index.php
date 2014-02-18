<?php

header('Content-type: application/json');
require('datamanip.php');
if (isset($_POST["SELECT_FUNCTION"])){
switch($_POST["SELECT_FUNCTION"])
{
    case 'getRequest';
        if (isset($_POST["ID1"])) {
         getRequest($_POST["ID1"]);}
    break;
    case 'getProfile';
        if (isset($_POST["ID"])){
            getProfile($_POST["ID"]);
        }
    break;
    case 'getPicture';
        if (isset($_POST["ID"])){
            getProfilePicture($_POST["ID"]);
        }
    break;
    case 'submitSubject';
        if (isset($_POST["ID"])&&isset($_POST["subject"])){
            submitSubject($_POST["ID"],$_POST["subject"]);
            updateArchiveSubject($_POST["ID"],$_POST["subject"]);
        }
    break;
    case 'getAllProfile';
            getAllProfile();
    break;
    case 'getLastSubject';
         if (isset($_POST["ID"])){
         getLastSubject ($_POST["ID"]);
         }
    break;
    default;
        echo 'Merci de faire un choix...';
    break;
}
}
?>					