package com.aetrion.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.photos.comments.Comment;
import com.aetrion.flickr.photos.comments.CommentsInterface;
import com.aetrion.flickr.util.IOUtilities;
/**
 * 
 * @author till (Till Krech) flickr:extranoise
 *
 */
public class CommentsInterfaceTest extends TestCase {

    Flickr flickr = null;
    Properties properties = null;
    
	public CommentsInterfaceTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/setup.properties");
            properties = new Properties();
            properties.load(in);

            REST rest = new REST();
            rest.setHost(properties.getProperty("host"));

            flickr = new Flickr(properties.getProperty("apiKey"), rest);

            RequestContext requestContext = RequestContext.getRequestContext();
            requestContext.setSharedSecret(properties.getProperty("secret"));

            AuthInterface authInterface = flickr.getAuthInterface();
            Auth auth = authInterface.checkToken(properties.getProperty("token"));
            requestContext.setAuth(auth);
        } finally {
            IOUtilities.close(in);
        }
	}
	
	public void testGetList() throws IOException, SAXException, FlickrException {
		String photoId = "245253195"; // http://www.flickr.com/photos/extranoise/245253195/
		CommentsInterface ci = new CommentsInterface(flickr.getApiKey(), flickr.getTransport());
		List comments = ci.getList(photoId);
		assertNotNull(comments);
		assertTrue(comments.size() > 0);
		Iterator commentsIterator = comments.iterator();
		
		while (commentsIterator.hasNext()) {
			Comment comment = (Comment)commentsIterator.next();
			assertNotNull(comment.getId());
			assertNotNull(comment.getAuthor());
			assertNotNull(comment.getAuthorName());
			assertNotNull(comment.getDateCreate());
			assertNotNull(comment.getPermaLink());
			assertNotNull(comment.getText());
			
		}
	}
	
	public void testComment() throws IOException, SAXException, FlickrException {
		String photoId = "4867789"; // http://flickr.com/photos/javatest/4867789/
		String txt1 = "This is a test for the flickr java api";
		String txt2 = "This is an edited comment for the java flickr api";
		CommentsInterface ci = new CommentsInterface(flickr.getApiKey(), flickr.getTransport());
		// add a comment
		String commentId = ci.addComment(photoId, txt1);
		System.out.println("Comment Id:" + commentId);
		assertNotNull(commentId);
		assertTrue(commentId.length() > 0);
		// verify if comment arrived on the photo page
		Comment comment = findCommment(photoId, commentId);
		assertNotNull(comment);
		assertEquals(commentId, comment.getId());
		assertEquals(txt1, comment.getText());
		// change the comment text and verify change
		ci.editComment(commentId, txt2);
		comment = findCommment(photoId, commentId);
		assertNotNull(comment);
		assertEquals(commentId, comment.getId());
		assertEquals(txt2, comment.getText());
		// delete the comment
		ci.deleteComment(commentId);
		comment = findCommment(photoId, commentId);
		assertNull(comment);
		
	}
	
	// helper function to find a comment by it's id for a specified photo
	private Comment findCommment(String photoId, String commentId) throws FlickrException, IOException, SAXException {
		CommentsInterface ci = new CommentsInterface(flickr.getApiKey(), flickr.getTransport());
		List comments = ci.getList(photoId);
		Iterator commentsIterator = comments.iterator();
		
		while (commentsIterator.hasNext()) {
			Comment comment = (Comment)commentsIterator.next();
			if (comment.getId().equals(commentId)) {
				return comment;
			}
		}
		return null;
		
	}
}