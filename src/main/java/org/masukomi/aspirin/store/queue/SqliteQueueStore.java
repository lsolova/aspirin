package org.masukomi.aspirin.store.queue;

import org.masukomi.aspirin.Aspirin;
import org.masukomi.aspirin.AspirinInternal;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>This class is a simple example of SQL based QueueStore implementation.</p>
 * <p>SQLite is selected, because it is a platform independent, file based, easy SQL system.</p>
 *
 * @author Laszlo Solova
 */
public class SqliteQueueStore implements QueueStore {

    public static final String PARAM_STORE_SQLITE_DB = "aspirin.store.sqlite.db";

    private Connection conn;

    public SqliteQueueStore() throws Exception {
        this((String) Aspirin.getConfiguration().getProperty(PARAM_STORE_SQLITE_DB));
    }

    SqliteQueueStore(String sqliteDbPath) throws Exception {
    if (sqliteDbPath == null)
      throw new Exception(
          "Store file is undefined. Please, check configuration.");
    // Initialize SQLite connection
    Class.forName("org.sqlite.JDBC");
    conn = DriverManager.getConnection("jdbc:sqlite:" + sqliteDbPath);
    conn.setAutoCommit(false);
    Statement stmt = conn.createStatement();
    stmt.execute("CREATE TABLE IF NOT EXISTS queueinfos (mailid VARCHAR(32), recipient TEXT, resultinfo TEXT, attempt BIGINT, attemptcount INT, expiry BIGINT, dstate SMALLINT)");
    stmt.execute("CREATE INDEX IF NOT EXISTS queueinfos_mailid_idx ON queueinfos (mailid)");
    stmt.execute("CREATE INDEX IF NOT EXISTS queueinfos_recipient_idx ON queueinfos (recipient)");
    stmt.execute("CREATE INDEX IF NOT EXISTS queueinfos_dstate_idx ON queueinfos (dstate)");
    stmt.execute("CREATE INDEX IF NOT EXISTS queueinfos_complexmr_idx ON queueinfos (mailid, recipient)");
    conn.commit();
      stmt.close();
    conn.setAutoCommit(true);
  }

  @Override
  public void add(String mailid, long expiry, Collection<InternetAddress> recipients) throws MessagingException {
      try {
      PreparedStatement pStmt = conn
          .prepareStatement("INSERT INTO queueinfos (mailid, recipient, resultinfo, attempt, attemptcount, expiry, dstate) VALUES (?,?,?,?,?,?,?)");
      for (InternetAddress recipient : recipients) {
        pStmt.setString(1, mailid);
        pStmt.setString(2, recipient.getAddress());
        pStmt.setNull(3, Types.CLOB);
        pStmt.setLong(4, System.currentTimeMillis());
        pStmt.setInt(5, 0);
        pStmt.setLong(6, expiry);
        pStmt.setInt(7, DeliveryState.QUEUED.getStateId());
        pStmt.addBatch();
      }
      int[] results = pStmt.executeBatch();
        pStmt.close();
      boolean allOkay = true;
      for (int r : results)
        if (r < 0) {
          allOkay = false;
        }
      if (results.length != recipients.size() || !allOkay) {
        throw new MessagingException(
            "Message queueing failed on prepared statement execution." + mailid);
      }
    } catch (Exception e) {
      throw new MessagingException("Message queueing failed: " + mailid, e);
    }
  }

  @Override
  public List<String> clean() {
    List<String> usedMailIds = new ArrayList<String>();
    try {
      executeSimpleQuery("DELETE FROM queueinfos WHERE mailid NOT IN (SELECT mailid FROM queueinfos WHERE dstate IN ("
          + DeliveryState.QUEUED.getStateId()
          + ", "
          + DeliveryState.IN_PROGRESS.getStateId() + "))");
      Statement stmt = conn.createStatement();
      ResultSet rS = stmt
          .executeQuery("SELECT DISTINCT mailid FROM queueinfos");
      if (rS != null) {
        while (rS.next())
          usedMailIds.add(rS.getString("mailid"));
          rS.close();
      }
      stmt.close();
      executeSimpleQuery("VACUUM");
    } catch (SQLException e) {
      AspirinInternal.getLogger().error("Store cleaning failed.", e);
    }
    return usedMailIds;
  }

  @Override
  public QueueInfo createQueueInfo() {
    return new QueueInfo();
  }

  @Override
  public long getNextAttempt(String mailid, String recipient) {
    PreparedStatement pStmt;
      int attempt = 0;
    try {
      pStmt = conn
          .prepareStatement("SELECT attempt FROM queueinfos WHERE mailid=? AND recipient=?");
      pStmt.setString(1, mailid);
      pStmt.setString(2, recipient);
      ResultSet rS = pStmt.executeQuery();
      if (rS != null && rS.next()) {
        Integer attemptResult = rS.getInt("attempt");
        if (0 < attemptResult)
          attempt = attemptResult;
          rS.close();
      }
        pStmt.close();
    } catch (SQLException e) {
      AspirinInternal.getLogger().error("Next attempt checking failed.", e);
    }
    return attempt;
  }

  @Override
  public boolean hasBeenRecipientHandled(String mailid, String recipient) {
    PreparedStatement pStmt;
      boolean recipientHandled = false;
    try {
      pStmt = conn
          .prepareStatement("SELECT dstate FROM queueinfos WHERE mailid=? AND recipient=?");
      pStmt.setString(1, mailid);
      pStmt.setString(2, recipient);
      ResultSet rS = pStmt.executeQuery();
      if (rS != null && rS.next()) {
        Integer dstate = rS.getInt("dstate");
        recipientHandled = dstate == DeliveryState.FAILED.getStateId() || dstate == DeliveryState.SENT
            .getStateId();
          rS.close();
      }
        pStmt.close();
    } catch (SQLException e) {
      AspirinInternal.getLogger().error(
          "Concrete delivery status checking (mailid '" + mailid
              + "' + recipient '" + recipient + "') failed.", e);
    }
    return recipientHandled;
  }

  @Override
  public void init() {
    try {
      executeSimpleQuery("UPDATE queueinfos SET dstate="
          + DeliveryState.QUEUED.getStateId() + " WHERE dstate="
          + DeliveryState.IN_PROGRESS.getStateId());
      AspirinInternal.getLogger().info("SQLite QueueStore initialized.");
    } catch (SQLException e) {
      AspirinInternal.getLogger().error(
          "SQLite QueueStore initialization failed.", e);
    }
  }

  @Override
  public boolean isCompleted(String mailid) {
      boolean completed = false;
      PreparedStatement pStmt;
      try {
        pStmt = conn
          .prepareStatement("SELECT COUNT(recipient) AS recipientcount FROM queueinfos WHERE mailid=? AND (dstate="
              + DeliveryState.QUEUED.getStateId()
              + " OR dstate="
              + DeliveryState.IN_PROGRESS.getStateId() + ")");
        pStmt.setString(1, mailid);
        ResultSet rS = pStmt.executeQuery();
      if (rS != null && rS.next()) {
        Integer rCount = rS.getInt("recipientcount");
        completed = rCount == 0;
          rS.close();
      }
        pStmt.close();
    } catch (SQLException e) {
          AspirinInternal.getLogger().error("Completion checking failed.", e);
      }
      return completed;
  }

  @Override
  public QueueInfo next() {
    try {
      synchronized (this) {

        /*
         * We can filter with a complex SQL query, which can check
         * sendable state of a QueueInfo, but this break our built-in
         * checks and feedback, so it is easier to get all items and
         * check it by Java code.
         * SELECT mailid, recipient, attempt, attemptcount, expiry FROM
         * queueinfos WHERE
         * attempt < 1306000972154 AND
         * attemptcount < 3 AND
         * dstate=2 AND
         * (expiry == -1 OR 1306000972154 < expiry )
         * ORDER BY attempt ASC LIMIT 1
         */
        PreparedStatement pStmt = conn
            .prepareStatement("SELECT mailid, recipient, attempt, attemptcount, expiry FROM queueinfos WHERE dstate=? ORDER BY attempt ASC LIMIT 100");
        pStmt.setInt(1, DeliveryState.QUEUED.getStateId());
        ResultSet rS = pStmt.executeQuery();
        if (rS != null) {
          while (rS.next()) {
            QueueInfo qi = new QueueInfo();
            qi.setAttempt(rS.getLong("attempt"));
            qi.setAttemptCount(rS.getInt("attemptcount"));
            qi.setExpiry(rS.getLong("expiry"));
            qi.setMailid(rS.getString("mailid"));
            qi.setRecipient(rS.getString("recipient"));
            qi.setState(DeliveryState.QUEUED);

            if (qi.isSendable()) {
              if (!qi.isInTimeBounds()) {
                qi.setResultInfo("Delivery is out of time or attempt.");
                qi.setState(DeliveryState.FAILED);
                setSendingResult(qi);
              }
              else {
                qi.setState(DeliveryState.IN_PROGRESS);
                executeSimplePreparedStatement(
                    "UPDATE queueinfos SET dstate=? WHERE mailid=? AND recipient=?",
                    DeliveryState.IN_PROGRESS.getStateId(), qi.getMailid(),
                    qi.getRecipient());
                return qi;
              }
            }
          }
            rS.close();
        }
          pStmt.close();
      }
    } catch (SQLException e) {
      AspirinInternal.getLogger().error(
          "Failed get next sendable queueinfo item.", e);
    }
    return null;
  }

  @Override
  public void remove(String mailid) {
    try {
      executeSimplePreparedStatement("DELETE FROM queueinfos WHERE mailid=?",
          mailid);
    } catch (SQLException e) {
      AspirinInternal.getLogger().error(
          "Removing by mailid failed. mailid=" + mailid, e);
    }
  }

  @Override
  public void removeRecipient(String recipient) {
    try {
      executeSimplePreparedStatement(
          "DELETE FROM queueinfos WHERE recipient=?", recipient);
    } catch (SQLException e) {
      AspirinInternal.getLogger().error(
          "Removing by recipient failed. recipient=" + recipient, e);
    }
  }

  @Override
  public void setSendingResult(QueueInfo qi) {
    try {
      executeSimplePreparedStatement(
          "UPDATE queueinfos SET resultinfo=?, attempt=?, attemptcount=attemptcount+1, dstate=? WHERE mailid=? AND recipient=?",
          qi.getResultInfo(), System.currentTimeMillis()
              + AspirinInternal.getConfiguration().getDeliveryAttemptDelay(),
          qi.getState().getStateId(), qi.getMailid(), qi.getRecipient());
      qi.setState(qi.getState());
    } catch (SQLException e) {
      AspirinInternal.getLogger().error("Sending result set failed. qi=" + qi,
          e);
    }
  }

  @Override
  public int size() {
    int size = 0;
    try {
      Statement stmt = conn.createStatement();
      ResultSet rS = stmt
          .executeQuery("SELECT COUNT(DISTINCT mailid) AS mcount FROM queueinfos");
      if (rS != null && rS.next()) {
        Integer mcount = rS.getInt("mcount");
        if (0 < mcount)
          size = mcount;
          rS.close();
      }
        stmt.close();
    } catch (SQLException e) {
      AspirinInternal.getLogger().error("Calculating queue size failed.", e);
    }
    return size;
  }

  private void executeSimpleQuery(String query) throws SQLException {
    Statement stmt = conn.createStatement();
    stmt.execute(query);
    stmt.close();
  }

  private void executeSimplePreparedStatement(String sql, Object... parameters)
      throws SQLException {
    PreparedStatement pStmt = conn.prepareStatement(sql);
    int i = 1;
    for (Object parameter : parameters) {
      if (parameter instanceof String)
        pStmt.setString(i, (String) parameter);
      else if (parameter instanceof Integer)
        pStmt.setInt(i, (Integer) parameter);
      else if (parameter instanceof Long)
        pStmt.setLong(i, (Long) parameter);
      i++;
    }
    pStmt.execute();
    pStmt.close();
  }

}
