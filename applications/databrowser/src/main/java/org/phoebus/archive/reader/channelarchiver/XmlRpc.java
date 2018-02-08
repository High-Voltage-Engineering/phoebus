/*******************************************************************************
 * Copyright (c) 2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.phoebus.archive.reader.channelarchiver;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.phoebus.framework.persistence.XMLUtil;
import org.w3c.dom.Element;

/** XML-RPC client
 *
 *  <p>Just the code needed to communicate with the archive data server.
 *  Avoids a third party library like Apache xmlrpc
 *  which needs patches to handle Double.NaN.
 *
 *  <p>Implemented with basic {@link URLConnection}.
 *  Could be updated to HttpClient once that moves beyond jdk.incubator.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XmlRpc
{
    /** Create XML-RPC request
     *  @param method
     *  @param params
     *  @return XML for the request
     *  @throws Exception on error
     */
    public static String command(final String method, final Object... params) throws Exception
    {
        final StringBuilder buf = new StringBuilder();
        buf.append("<?xml version=\"1.0\" encoding=\"").append(XMLUtil.ENCODING).append("\"?>\n");
        buf.append("<methodCall>\n");
        buf.append(" <methodName>").append(URLEncoder.encode(method, XMLUtil.ENCODING)).append("</methodName>\n");
        if (params.length > 0)
        {
            buf.append(" <params>\n");
            for (Object param : params)
            {
                buf.append("  <param>\n");
                buf.append("   <value>");
                if (param instanceof Integer)
                    buf.append("<i4>").append(param).append("</i4>\n");
                else if (param instanceof String)
                    buf.append("<string>").append(param).append("</string>\n");
                else
                    throw new Exception("Cannot handle parameter of type " + param.getClass().getName());
                buf.append("   </value>\n");
                buf.append("  </param>\n");
            }
            buf.append(" </params>\n");
        }
        buf.append("</methodCall>\n");
        return buf.toString();
    }

    /** Send XML-RPC command, retrieve response
     *
     *  @param url Server URL
     *  @param command Command to send
     *  @return "methodResponse" from the reply
     *  @throws Exception on error
     */
    public static Element communicate(final URL url, final String command) throws Exception
    {
        final URLConnection connection = url.openConnection();
        connection.setDoOutput(true);

        final OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write(command);
        out.flush();
        out.close();

        return XMLUtil.openXMLDocument(connection.getInputStream(), "methodResponse");
    }

    /** Get expected XML child element
     *  @param parent XML parent
     *  @param name Child element name
     *  @return Child element
     *  @throws Exception if not found
     */
    public static Element getChildElement(final Element parent, final String name) throws Exception
    {
        final Element result = XMLUtil.getChildElement(parent, name);
        if (result == null)
            throw new Exception("Expected XML element <" + name + ">");
        return result;
    }

    /** @param value A "value" node that contains a "struct"
     *  @param name Name of desired structure member
     *  @return "value" node of that member
     *  @throws Exception on error
     */
    public static Element getStructMember(final Element value, final String name) throws Exception
    {
        final Element struct = XMLUtil.getChildElement(value,  "struct");
        for (Element member : XMLUtil.getChildElements(struct, "member"))
        {
            if (name.equals(XMLUtil.getChildString(member, "name").orElse(null)))
                return getChildElement(member, "value");
        }
        throw new Exception("Cannot locate struct element <" + name + ">");
    }

    /** @param value A "value" node that contains an "array"
     *  @return Iterator for the "value" nodes within the array
     *  @throws Exception on error
     */
    public static Iterable<Element> getArrayValues(final Element value) throws Exception
    {
        Element el = getChildElement(value, "array");
        el = getChildElement(el, "data");
        return XMLUtil.getChildElements(el, "value");
    }

    /** @param value A "value" node that contains "string", "i4"
     *  @return {@link String}, {@link Integer}
     *  @throws Exception on error
     */
    public static <TYPE> TYPE getValue(final Element value) throws Exception
    {
        final Element content = (Element) value.getFirstChild();
        final String type = content.getNodeName();
        if ("string".equals(type))
            return (TYPE) XMLUtil.getString(content);
        else if ("i4".equals(type))
            return (TYPE) Integer.valueOf(XMLUtil.getString(content));
        else
            throw new Exception("Cannot decode type " + type);
    }

}
