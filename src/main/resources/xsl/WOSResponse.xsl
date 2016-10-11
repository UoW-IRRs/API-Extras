<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:response="http://www.isinet.com/xrpc42">

    <xsl:output method="xml" indent="yes" version="1.0" encoding="UTF-8" omit-xml-declaration="yes"/>

    <xsl:param name="last-updated"/>
    <xsl:param name="service"/>
    <xsl:param name="prefix" select="'10289'"/>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="response:response">
        <data last-updated="{$last-updated}" service="{$service}">
            <xsl:apply-templates/>
        </data>
    </xsl:template>

    <xsl:template match="response:fn[@rc = 'OK']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="response:fn/response:map">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="response:map[@name][not(*/response:val[@name='message'])]">
        <item>
            <xsl:attribute name="handle">
                <xsl:value-of select="translate(@name, '_', '/')"/>
            </xsl:attribute>
            <xsl:apply-templates select="*/response:val"/>
        </item>
    </xsl:template>

    <xsl:template match="response:val[@name = 'timesCited']">
        <citations>
            <xsl:value-of select="normalize-space(text())"/>
        </citations>
    </xsl:template>

    <xsl:template match="response:val[@name = 'sourceURL']">
        <link>
            <xsl:value-of select="normalize-space(text())"/>
        </link>
    </xsl:template>

    <xsl:template match="response:val[@name = 'citingArticlesURL']">
        <citations-link>
            <xsl:value-of select="normalize-space(text())"/>
        </citations-link>
    </xsl:template>

    <xsl:template match="*"></xsl:template>

</xsl:stylesheet>