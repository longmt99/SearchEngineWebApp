CREATE DATABASE `searchengine` /*!40100 DEFAULT CHARACTER SET utf8 */;

CREATE TABLE `indexfile` (
  `word` varchar(30) NOT NULL,
  `docNumber` varchar(500) NOT NULL DEFAULT '',
  `freq` varchar(500) DEFAULT '',
  `hits` int(4) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `postingfile` (
  `docPath` varchar(300) NOT NULL,
  `docNumber` int(4) NOT NULL AUTO_INCREMENT,
  `deleted` int(1) DEFAULT '0',
  `lastIndex` bigint(20) DEFAULT '0',
  `isPicture` tinyint(1) NOT NULL DEFAULT '0',
  `tagNames` varchar(300) NOT NULL DEFAULT '',
  PRIMARY KEY (`docNumber`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
