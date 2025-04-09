update initiation_info
  set status = '200' where status = '200 OK';
  
update initiation_info
  set status = '404' where status = '404 Not Found';
  
update initiation_info
  set status = '406' where status = '406 Not Acceptable';

update initiation_info
  set status = '500' where status = '500 Internal Server Error';
