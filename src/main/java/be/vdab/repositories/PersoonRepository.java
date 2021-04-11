package be.vdab.repositories;

import be.vdab.domain.Gezin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PersoonRepository extends AbstractRepository{
    public void creerEenGezin(Gezin gezin) throws SQLException{
        var insertOuder="INSERT INTO personen(voornaam) VALUES (?)";
        var insertKind = "INSERT INTO personen(voornaam,papaId,mamaId) VALUES (?,?,?)";
        try (var connection = super.getConnection();
             var statementOuder = connection.prepareStatement(insertOuder, PreparedStatement.RETURN_GENERATED_KEYS)){
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            connection.setAutoCommit(false);
            statementOuder.setString(1, gezin.getPapa());
            statementOuder.addBatch();
            statementOuder.setString(1, gezin.getMama());
            statementOuder.addBatch();
            statementOuder.executeBatch();
            var result = statementOuder.getGeneratedKeys();
            result.next();
            var papaId = result.getLong(1);
            result.next();
            var mamaId = result.getLong(1);
            try (var statementKind = connection.prepareStatement(insertKind)) {
                statementKind.setLong(2, papaId);
                statementKind.setLong(3, mamaId);
                for (String kind : gezin.getKinderen()) {
                    statementKind.setString(1, kind);
                    statementKind.addBatch();
                }
                statementKind.executeBatch();
            }
            connection.commit();
        }
    }
}
