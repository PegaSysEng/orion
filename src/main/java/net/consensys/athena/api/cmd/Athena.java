package net.consensys.athena.api.cmd;

import static io.vertx.core.Vertx.vertx;

import net.consensys.athena.api.config.Config;
import net.consensys.athena.api.enclave.KeyConfig;
import net.consensys.athena.api.network.NetworkNodes;
import net.consensys.athena.impl.cmd.AthenaArguments;
import net.consensys.athena.impl.config.TomlConfigBuilder;
import net.consensys.athena.impl.enclave.sodium.SodiumFileKeyStore;
import net.consensys.athena.impl.http.data.Serializer;
import net.consensys.athena.impl.http.server.vertx.VertxServer;
import net.consensys.athena.impl.network.MemoryNetworkNodes;

import java.io.*;
import java.util.Optional;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Athena {

  private static final Logger log = LogManager.getLogger();

  private static NetworkNodes networkNodes;
  private static final Serializer serializer = new Serializer();

  private static final Vertx vertx = vertx();

  public static void main(String[] args) throws Exception {
    log.info("starting athena");
    Athena athena = new Athena();
    athena.run(args);
  }

  public void run(String[] args) throws FileNotFoundException, InterruptedException {
    // parsing arguments
    AthenaArguments arguments = new AthenaArguments(args);

    if (arguments.argumentExit()) {
      return;
    }

    // load config file
    Config config = loadConfig(arguments.configFileName());

    // generate key pair and exit
    if (arguments.keysToGenerate().isPresent()) {

      log.info("generating Key Pairs");

      SodiumFileKeyStore keyStore = new SodiumFileKeyStore(config, serializer);

      for (int i = 0; i < arguments.keysToGenerate().get().length; i++) {
        keyStore.generateKeyPair(
            new KeyConfig(arguments.keysToGenerate().get()[i], Optional.empty()));
      }
      return;
    }

    // start our API server
    networkNodes = new MemoryNetworkNodes(config);

    AthenaRouter router = new AthenaRouter(vertx, networkNodes, config, serializer);
    VertxServer httpServer = new VertxServer(vertx, router.getRouter(), config);
    httpServer.start();
  }

  Config loadConfig(Optional<String> configFileName) throws FileNotFoundException {
    InputStream configAsStream;
    if (configFileName.isPresent()) {
      log.info("using {} provided config file", configFileName.get());
      configAsStream = new FileInputStream(new File(configFileName.get()));
    } else {
      log.warn("no config file provided, using default.conf");
      configAsStream = Athena.class.getResourceAsStream("/default.conf");
    }

    TomlConfigBuilder configBuilder = new TomlConfigBuilder();

    return configBuilder.build(configAsStream);
  }
}
