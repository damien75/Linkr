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
    default;
        echo 'Merci de faire un choix...';
    break;
}
}

?>									