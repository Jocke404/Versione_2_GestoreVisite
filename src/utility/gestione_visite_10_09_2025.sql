-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 10, 2025 at 06:39 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `gestione_visite`
--

-- --------------------------------------------------------

--
-- Table structure for table `configuratori`
--

CREATE TABLE `configuratori` (
  `nome` varchar(255) NOT NULL,
  `cognome` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `password_modificata` tinyint(4) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `configuratori`
--

INSERT INTO `configuratori` (`nome`, `cognome`, `email`, `password`, `password_modificata`) VALUES
('Admin', 'Configuratore', 'admin@example.com', 'admin123', 0),
('Super', 'User', 'superuser@example.com', 'super456', 0);

-- --------------------------------------------------------

--
-- Table structure for table `credenziali_temporanee`
--

CREATE TABLE `credenziali_temporanee` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `credenziali_temporanee`
--

INSERT INTO `credenziali_temporanee` (`id`, `username`, `password`) VALUES
(1, 'tempuser1', 'temppass1'),
(2, 'tempuser2', 'temppass2'),
(3, 'tempuser3', 'temppass3');

-- --------------------------------------------------------

--
-- Table structure for table `date_precluse`
--

CREATE TABLE `date_precluse` (
  `id` int(11) NOT NULL,
  `data` date NOT NULL,
  `motivo` varchar(255) NOT NULL,
  `creato_il` timestamp NOT NULL DEFAULT current_timestamp(),
  `modificato_il` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `date_precluse`
--

INSERT INTO `date_precluse` (`id`, `data`, `motivo`, `creato_il`, `modificato_il`) VALUES
(1, '2024-12-25', 'Natale', '2025-08-22 17:23:23', '2025-08-22 17:23:23'),
(2, '2024-12-26', 'Santo Stefano', '2025-08-22 17:23:23', '2025-08-22 17:23:23'),
(3, '2025-01-01', 'Capodanno', '2025-08-22 17:23:23', '2025-08-22 17:23:23'),
(4, '2025-04-25', 'Liberazione', '2025-08-22 17:23:23', '2025-08-22 17:23:23'),
(5, '2025-05-01', 'Festa del Lavoro', '2025-08-22 17:23:23', '2025-08-22 17:23:23'),
(6, '2025-08-15', 'Ferragosto', '2025-08-22 17:23:23', '2025-08-22 17:23:23'),
(8, '2026-12-23', 'Compleanno Amore', '2025-08-28 18:32:40', '2025-08-28 18:32:40'),
(9, '2026-01-10', 'Anniversario fidanzamento', '2025-08-28 19:11:24', '2025-08-28 19:11:24');

-- --------------------------------------------------------

--
-- Table structure for table `fruitori`
--

CREATE TABLE `fruitori` (
  `id` int(11) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `cognome` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `password_modificata` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fruitori`
--

INSERT INTO `fruitori` (`id`, `nome`, `cognome`, `email`, `password`, `password_modificata`) VALUES
(1, 'Ruggero', 'Lombardi', 'r.lombardi002@studenti.unibs.it', 'Test', 0);

-- --------------------------------------------------------

--
-- Table structure for table `luoghi`
--

CREATE TABLE `luoghi` (
  `nome` varchar(255) NOT NULL,
  `descrizione` text NOT NULL,
  `collocazione` varchar(255) DEFAULT NULL,
  `tipi_di_visita` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `luoghi`
--

INSERT INTO `luoghi` (`nome`, `descrizione`, `collocazione`, `tipi_di_visita`) VALUES
('Base Aeronautica', 'LoremIpsum', 'Ghedi, BS', 'SCIENTIFICA,STORICA'),
('Brescia', 'Citt� fantastica', 'Brescia Centro', 'STORICA,ENOGASTRONOMICA'),
('CASA TEST', '555555', '555', 'STORICA,ENOGASTRONOMICA'),
('Castello di Brescia', 'Alla scoperta del fantastico Castello di Brescia', 'Colle Cidneo, BS', 'STORICA,LABBAMBINI'),
('Castello Storico', 'Un castello medievale ben conservato.', 'Montichiari, BS', 'STORICA'),
('Ghedi', 'Citta\' fantastica', 'Citta\' di Ghedi', 'ENOGASTRONOMICA,SCIENTIFICA'),
('Montichiari', 'LoremIpsum', 'Montichiari, BS', 'ENOGASTRONOMICA,STORICA'),
('Test', 'Test', 'Posizione Test', 'LABBAMBINI,SCIENTIFICA,STORICA');

-- --------------------------------------------------------

--
-- Table structure for table `prenotazioni`
--

CREATE TABLE `prenotazioni` (
  `id` int(11) NOT NULL,
  `id_visita` int(11) NOT NULL,
  `email_fruitore` varchar(255) NOT NULL,
  `numero_persone` int(11) NOT NULL CHECK (`numero_persone` >= 1),
  `data_prenotazione` timestamp NOT NULL DEFAULT current_timestamp(),
  `codice_prenotazione` varchar(20) NOT NULL,
  `stato` enum('CONFERMATA','CANCELLATA','COMPLETATA') DEFAULT 'CONFERMATA'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `prenotazioni`
--

INSERT INTO `prenotazioni` (`id`, `id_visita`, `email_fruitore`, `numero_persone`, `data_prenotazione`, `codice_prenotazione`, `stato`) VALUES
(1, 17, 'r.lombardi002@studenti.unibs.it', 2, '2025-09-10 16:26:20', 'PRN1757521580844341', 'CONFERMATA'),
(2, 13, 'r.lombardi002@studenti.unibs.it', 5, '2025-09-10 16:28:20', 'PRN1757521700585510', 'CONFERMATA');

-- --------------------------------------------------------

--
-- Table structure for table `utenti_unificati`
--

CREATE TABLE `utenti_unificati` (
  `nome` varchar(255) NOT NULL,
  `cognome` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `tipo_utente` varchar(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_modificata` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `utenti_unificati`
--

INSERT INTO `utenti_unificati` (`nome`, `cognome`, `email`, `password`, `tipo_utente`, `password_modificata`) VALUES
('Admin', 'Configuratore', 'admin@example.com', 'admin123', 'Configuratore', 0),
('Alessandro', 'Neri', 'alessandro.neri@example.com', 'password321', 'Volontario', 0),
('Config', 'Test', 'configtest@example.com', 'passconftest123', 'Configuratore', 0),
('Francesca', 'Gialli', 'francesca.gialli@example.com', 'password654', 'Volontario', 0),
('Giulia', 'Verdi', 'giulia.verdi@example.com', 'passmodificata789', 'Volontario', 1),
('Luisa', 'Bianchi', 'luisa.bianchi@example.com', 'passmodificata456', 'Volontario', 1),
('Mario', 'Rossi', 'mario.rossi@example.com', 'passmodificata123', 'Volontario', 1),
('Ruggero', 'Lombardi', 'r.lombardi002@studenti.unibs.it', 'Test', 'Fruitore', 0),
('Super', 'User', 'superuser@example.com', 'super456', 'Configuratore', 0),
('Nome Temporaneo', 'Cognome Temporaneo', 'tempuser1', 'temppass1', 'TEMP', 1),
('Nome Temporaneo', 'Cognome Temporaneo', 'tempuser2', 'temppass2', 'TEMP', 1),
('Nome Temporaneo', 'Cognome Temporaneo', 'tempuser3', 'temppass3', 'TEMP', 1),
('TestAdd', 'Add', 'Test@gmail.com', '12345', 'Volontario', 0);

-- --------------------------------------------------------

--
-- Table structure for table `visite`
--

CREATE TABLE `visite` (
  `id` int(11) NOT NULL,
  `luogo` varchar(255) DEFAULT NULL,
  `tipo_visita` varchar(255) DEFAULT NULL,
  `volontario` varchar(255) DEFAULT NULL,
  `data` date DEFAULT NULL,
  `max_persone` int(11) DEFAULT 10,
  `stato` varchar(255) NOT NULL DEFAULT 'Proposta',
  `ora_inizio` time DEFAULT NULL,
  `durata_minuti` int(11) DEFAULT 60,
  `posti_prenotati` int(11) DEFAULT 0
) ;

--
-- Dumping data for table `visite`
--

INSERT INTO `visite` (`id`, `luogo`, `tipo_visita`, `volontario`, `data`, `max_persone`, `stato`, `ora_inizio`, `durata_minuti`, `posti_prenotati`) VALUES
(10, 'Montichiari', 'ENOGASTRONOMICA', 'Mario Rossi', '2025-03-31', 35, 'Proposta', '10:00:00', 150, 0),
(12, 'Brescia', 'ENOGASTRONOMICA', 'Mario Rossi', '2025-03-31', 35, 'Proposta', '11:30:00', 150, 0),
(13, 'Ghedi', 'SCIENTIFICA', 'Mario Rossi', '2025-03-31', 35, 'Proposta', '15:00:00', 120, 5),
(17, 'Brescia', 'STORICA', 'TestAdd Add', '2025-11-03', 35, 'Confermata', '09:30:00', 90, 2),
(18, 'Ghedi', 'STORICA', 'Alessandro Neri', '2025-11-14', 35, 'Confermata', '14:00:00', 90, 0),
(20, 'Ghedi', 'ENOGASTRONOMICA', 'TestAdd Add', '2025-11-28', 35, 'Proposta', '16:30:00', 150, 0),
(23, 'Ghedi', 'ENOGASTRONOMICA', 'TestAdd Add', '2025-11-28', 35, 'Proposta', '13:30:00', 90, 0),
(24, 'Ghedi', 'ENOGASTRONOMICA', 'TestAdd Add', '2025-11-28', 35, 'Proposta', '12:00:00', 90, 0),
(25, 'Ghedi', 'ENOGASTRONOMICA', 'TestAdd Add', '2025-11-28', 35, 'Proposta', '09:00:00', 90, 0),
(26, 'CASA TEST', 'STORICA', 'Francesca Gialli', '2025-12-01', 35, 'Proposta', '09:00:00', 120, 0),
(27, 'CASA TEST', 'ENOGASTRONOMICA', 'Mario Rossi', '2025-12-01', 35, 'Proposta', '11:00:00', 60, 0);

-- --------------------------------------------------------

--
-- Table structure for table `volontari`
--

CREATE TABLE `volontari` (
  `nome` varchar(255) NOT NULL,
  `cognome` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `tipi_di_visite` text DEFAULT NULL,
  `password_modificata` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `volontari`
--

INSERT INTO `volontari` (`nome`, `cognome`, `email`, `password`, `tipi_di_visite`, `password_modificata`) VALUES
('Alessandro', 'Neri', 'alessandro.neri@example.com', 'password321', 'SCIENTIFICA, STORICA, LABBAMBINI', 0),
('Francesca', 'Gialli', 'francesca.gialli@example.com', 'password654', 'LABBAMBINI, STORICA', 0),
('Giulia', 'Verdi', 'giulia.verdi@example.com', 'passmodificata789', 'STORICA', 1),
('Luisa', 'Bianchi', 'luisa.bianchi@example.com', 'passmodificata456', 'SCIENTIFICA', 1),
('Mario', 'Rossi', 'mario.rossi@example.com', 'passmodificata123', 'STORICA, SCIENTIFICA, ENOGASTRONOMICA, LABBAMBINI', 1),
('TestAdd', 'Add', 'Test@gmail.com', '12345', '', 0);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `configuratori`
--
ALTER TABLE `configuratori`
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `credenziali_temporanee`
--
ALTER TABLE `credenziali_temporanee`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `date_precluse`
--
ALTER TABLE `date_precluse`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_data` (`data`);

--
-- Indexes for table `fruitori`
--
ALTER TABLE `fruitori`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `luoghi`
--
ALTER TABLE `luoghi`
  ADD PRIMARY KEY (`nome`);

--
-- Indexes for table `prenotazioni`
--
ALTER TABLE `prenotazioni`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codice_prenotazione` (`codice_prenotazione`),
  ADD KEY `id_visita` (`id_visita`),
  ADD KEY `fk_prenotazioni_fruitori_email` (`email_fruitore`);

--
-- Indexes for table `utenti_unificati`
--
ALTER TABLE `utenti_unificati`
  ADD PRIMARY KEY (`email`);

--
-- Indexes for table `visite`
--
ALTER TABLE `visite`
  ADD PRIMARY KEY (`id`),
  ADD KEY `luogo` (`luogo`);

--
-- Indexes for table `volontari`
--
ALTER TABLE `volontari`
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `credenziali_temporanee`
--
ALTER TABLE `credenziali_temporanee`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `date_precluse`
--
ALTER TABLE `date_precluse`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `fruitori`
--
ALTER TABLE `fruitori`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `prenotazioni`
--
ALTER TABLE `prenotazioni`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `visite`
--
ALTER TABLE `visite`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `prenotazioni`
--
ALTER TABLE `prenotazioni`
  ADD CONSTRAINT `fk_prenotazioni_fruitori_email` FOREIGN KEY (`email_fruitore`) REFERENCES `fruitori` (`email`) ON DELETE CASCADE,
  ADD CONSTRAINT `prenotazioni_ibfk_1` FOREIGN KEY (`id_visita`) REFERENCES `visite` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `visite`
--
ALTER TABLE `visite`
  ADD CONSTRAINT `visite_ibfk_1` FOREIGN KEY (`luogo`) REFERENCES `luoghi` (`nome`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
