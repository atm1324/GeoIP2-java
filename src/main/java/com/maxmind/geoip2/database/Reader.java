package com.maxmind.geoip2.database;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CountryLookup;
import com.maxmind.geoip2.model.OmniLookup;

/**
 * Instances of this class provide a reader for the GeoIP2 database format. IP
 * addresses can be looked up using the <code>get</code> method.
 */
public class Reader implements Closeable {

    // This is sort of annoying. Rename one of the two?
    private final com.maxmind.maxminddb.Reader reader;

    private final ObjectMapper om;

    /**
     * Constructs a Reader for the GeoIP2 database format. The file passed to it
     * must be a valid GeoIP2 database file.
     * 
     * @param database
     *            the GeoIP2 database file to use.
     * @throws IOException
     *             if there is an error opening or reading from the file.
     */
    public Reader(File database) throws IOException {
        this(database, Arrays.asList("en"));
    }

    /**
     * Constructs a Reader for the GeoIP2 database format. The file passed to it
     * must be a valid GeoIP2 database file.
     * 
     * @param database
     *            the GeoIP2 database file to use.
     * @param languages
     *            List of language codes to use in name property from most
     *            preferred to least preferred.
     * @throws IOException
     *             if there is an error opening or reading from the file.
     */
    public Reader(File database, List<String> languages) throws IOException {
        this.reader = new com.maxmind.maxminddb.Reader(database);
        this.om = new ObjectMapper();
        this.om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        InjectableValues inject = new InjectableValues.Std().addValue(
                "languages", languages);
        this.om.setInjectableValues(inject);
    }

    /**
     * @param ipAddress
     *            IPv4 or IPv6 address to lookup.
     * @return A <T> object with the data for the IP address
     * @throws IOException
     *             if there is an error opening or reading from the file.
     * @throws AddressNotFoundException
     *             if the IP address is not in our database
     */
    public <T extends CountryLookup> T get(InetAddress ipAddress)
            throws IOException, AddressNotFoundException {
        ObjectNode node = (ObjectNode) this.reader.get(ipAddress);

        // XXX - I am not sure Java programmers would expect an exception here,
        // but the web service code does throw an exception in this case. If we
        // keep this exception, we should adjust the web service to throw the
        // same exception when it gets and IP_ADDRESS_NOT_FOUND error.
        if (node == null) {
            throw new AddressNotFoundException("The address "
                    + ipAddress.getHostAddress() + " is not in the database.");
        }

        if (!node.has("traits")) {
            node.put("traits", this.om.createObjectNode());
        }
        ObjectNode traits = (ObjectNode) node.get("traits");
        traits.put("ip_address", ipAddress.getHostAddress());

        // The cast and the OmniLookup.class are sort of ugly. There might be a
        // better way
        return (T) this.om.treeToValue(node, OmniLookup.class);
    }

    /**
     * Closes the GeoIP2 database and returns resources to the system.
     * 
     * @throws IOException
     *             if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        this.reader.close();
    }
}