<?php

function insertProfilePicture($url,$target){
   $file = file_get_contents($url);
   $img = imagecreatefromstring($file);
   imagejpeg($img, "./images/original/".$target.".jpg", 100);
   list($width,$height,$type,$html) = getimagesizefromstring($file);
   $newWidth = 200;
   $newHeight = (int) ($height * $newWidth / $width);
   $smallWidth = 100;
   $smallHeight = (int)($height * $smallWidth / $width);
   $newImage = imagecreatetruecolor($newWidth, $newHeight);
   imagecopyresampled($newImage, $img, 0, 0, 0, 0, $newWidth, $newHeight, $width, $height);
   imagejpeg($newImage,"./images/standard/".$target.".jpg",100);
   $smallImage = imagecreatetruecolor($smallWidth, $smallHeight);
   imagecopyresampled($smallImage, $img, 0, 0, 0, 0, $smallWidth, $smallHeight, $width, $height);
   imagejpeg($smallImage,"./images/thumbnail/".$target.".jpg",100);
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
?>
