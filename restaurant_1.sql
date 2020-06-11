DROP SCHEMA restaurant;

CREATE DATABASE restaurant;
use restaurant;

#식당정보
CREATE TABLE store(
indexNo CHAR(5) NOT NULL, #인덱스 번호
storeNumber CHAR(5) NOT NULL, # 음식점번호		
storeName VARCHAR(20) NOT NULL, # 음식점이름		
delivery VARCHAR(3) NOT NULL, #배달가능여부
emptyTable INT(2) NOT NULL, #빈자리의 수
watingNumber INT(2) NOT NULL, #기다리고 있는 인원 수
location  VARCHAR(50) NOT NULL, #음식점의 위치
PRIMARY KEY (indexNo)
);
#음식점별 메뉴
CREATE TABLE menu(
indexNo CHAR(5) NOT NULL, #인덱스 번호
menuId CHAR(5) NOT NULL, #메뉴별 인덱스
menuName VARCHAR(50) NOT NULL, #메뉴 이름
price INT(10) NOT NULL, #가격
PRIMARY KEY (menuId),
FOREIGN KEY(indexNo) REFERENCES store (indexNo)
);
#예약 및 주문
CREATE TABLE reservation(
indexNo CHAR(5) NOT NULL, #인덱스 번호
resNo CHAR(5) NOT NULL, #예약번호
userId VARCHAR(10) NOT NULL, #주문자의 아이디
userLocation VARCHAR(50) NOT NULL, #주문자의 거주위치
userPhone INT(11) NOT NULL,	#주문자의 전화번호
userNumber INT(3) NOT NULL, #주문자의 수
PRIMARY KEY (resNo),
FOREIGN KEY(indexNo) REFERENCES menu(indexNo),
FOREIGN KEY(indexNo) REFERENCES store(indexNo)
);

