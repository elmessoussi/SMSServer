<?php

  $link = mysql_connect("localhost", "login", "password");
  if (!$link) {
      die('<br/>Connexion impossible : ' );
  }
  $db_selected = mysql_select_db("DBName");
  if (!$db_selected) {
     die ('Impossible de sélectionner la base de données : ' );
  }
  mysql_query("SET NAMES 'utf8'");
  $sql="SELECT * FROM `sms`";
  $res = mysql_query($sql);

  while ($row = mysql_fetch_assoc($res)) {
      $rows[] = $row;
  }

  // JSON-ify all rows together as one big array
  echo json_encode($rows);

  mysql_close($mysql);
	
?>
