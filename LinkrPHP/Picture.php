<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('allow_url_fopen',TRUE);
require('datamanip.php');

if (isset($_GET["SELECT_FUNCTION"])) {
    switch ($_GET["SELECT_FUNCTION"]) {
        case 'getProfilePicture';
            if (isset($_GET["ID"])) {
                getProfilePicture($_GET["ID"]);
            }
            break;
        case 'insertPicture';
            if (isset($_GET["ID"]) &&isset($_GET["link"])){
               insertPicture($_GET["link"],$_GET["ID"]);
            }
            break;
        default;
           echo 'Merci de faire un choix';
           break;
}
}
?>