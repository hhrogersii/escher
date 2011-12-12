import processing.core.*; 
import processing.xml.*; 

import com.processinghacks.arcball.*; 
import processing.opengl.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class sketch_Eschers_Knot extends PApplet {

  //-----------------//
  // Escher's Knot   //
  // by Henry Rogers //
  // 5/4/2011 - v1.0 //
  //-----------------//
  
  // Key Controls: c - cycle; r - revolve; g - gamut; w - wireframe; mouse click drag - rotate.
  
  //---- globals
  int _e = 43;   // for elizabeth
  int _p = 576;  // number of points on curve
  int _n = 72;   // number of boxes or key frames along curve
  int _u = 8;    // number of tweens per key frame
  int _b = 4;    // number of points in width of box
  int _a = 2;    // curve scaling factor 
  
  int _c = 0;    // to cycle or not to cycle
  int _f = 0;    // frame iterator for cycling
  int _g = 0;    // to show gamut or not
  int _h = 0;    // hue interator for gamut
  int _o = 36;   // original hue for tube
  int _r = 0;    // to revolve or not to revolve
  int _m = 0;    // mobius iterator for rotation
  int _w = 0;    // to show wireframe or solids
  
  int _x = 90;   // initial x rotation
  int _y = 70;   // initial y rotation
  int _z = 90;   // initial z rotation
  int _s = 72;   // initial model scale 

  float _d1 = 0.38f; // distance from corner to first midpoint on side
  float _d2 = 0.62f; // distance from corner to second midpoint on side

  boolean _l = false; // lock hover state when button is pressed
    
  PImage _t; // texture image

  TrefoilCurve _k; // Trefoil knot class

  // button controls
  Button btnCycle;     // start/stop cycling
  Button btnRevolve;   // start/stop revolution
  Button btnGamut;     // show/hide gamut
  Button btnWireframe; // show/hide wireframe


  public void setup()
  {
    
    
    if (online==false)
    {
      
      size( 760, 760, OPENGL );
      smooth();
      textFont( loadFont("data/font.vlw"), 18 );
    }
    else
    {
      size( 760, 760, P3D );
      textMode(SCREEN);
    }

    colorMode( HSB, 360, 100, 100 );

    // camera control
    ArcBall arcball = new ArcBall( this );
    
    _t = loadImage( "data/lines.jpg" );
    textureMode( NORMALIZED );

    // instantiate trefoil curve
    _k = new TrefoilCurve( _a, _d1, _d2 ); 

    // instantiate button controls
    btnCycle = new Button( 6, 6, color(50,40,80), "Cycle" ); 
    btnRevolve = new Button( btnCycle.right() + 3, 6, color(50,40,80), "Revolve" );
    btnGamut = new Button( btnRevolve.right() + 3, 6, color(50,40,80), "Gamut" ); 
    btnWireframe = new Button( btnGamut.right() + 3, 6, color(50,40,80), "Wireframe" ); 
  }

  public void draw()
  {
    background( 59, 7, 85 );
    lights();
    
    // adjust lighting depending on gamut or texture mode
    if ( _g == 1 )
    {
      lightSpecular( 30, 40, 50 );
      directionalLight( 0, 0, 20, 0, 0, -1 ); 
    }
    else
    {
      lightSpecular( 25, 50, 72 );
      pointLight( 25, 50, 100, 0, 0, -12 );
      directionalLight( 25, 50, 72, 0, 0, -1 );
    }
    
    translate( width/2, height/2, -height/2 );
    rotateX( radians( _x ) ); 
    rotateY( radians( _y ) );
    rotateZ( radians( _z ) );
    scale( _s );

    int i,j;
    
    // advance revolution angle by mobius iterator
    float mobius = _m * TWO_PI / (_p/6); 

    // loop through key frames on curve
    for( j=0; j<_p; j=j+_u )
    {
      i = j + _f;  // advance tube by frame iterator

      // draw sides of tube
      drawTube( i, HALF_PI * 0 + mobius );
      drawTube( i, HALF_PI * 1 + mobius );
      drawTube( i, HALF_PI * 2 + mobius );
      drawTube( i, HALF_PI * 3 + mobius );
    }
    
    // persist control iterators
    _f = ( _f + _c ) % _u;     // advance frame iterator if cycling
    _h = ( _h + _c ) % 360;    // advance hue iterator if cycling
    _m = ( _m + _r ) % (_p/6); // advance mobius iterator if revolving

    // gui io
    drawGUI();
  }

/*
Trefoil knot:
  x = sin(t) + 2sin(2t)
  y = sin(3t)
  z = cos(t) - 2cos(2t)
  0 <= t <= 2Pi 
*/
// parameterization in 1995 PhD thesis of Aaron Trautwein at The University of Iowa is right hand trefoil

class TrefoilCurve 
{
  float _scalar = 0.0f;    // curve scaling factor
  float _distance1 = 0.0f; // distance from corner to first midpoint on side
  float _distance2 = 0.0f; // distance from corner to second midpoint on side

  //---- constructor
  
  TrefoilCurve( int scalar, float midpoint1, float midpoint2 ) 
  { 
    _scalar = scalar;
    _distance1 = midpoint1;
    _distance2 = midpoint2;
  }


  //---- derivatives 
  
  // calculate first differential
  private PVector dtFirst( float t )
  { 
    float x = _scalar * ( 3 * cos(3*t) );
    float y = _scalar * ( 4 * cos(2*t) + cos(t) );
    float z = _scalar * ( 4 * sin(2*t) - sin(t) );

    return new PVector( x, y, z ) ;
  }

  // calculate second differential
  private PVector dtSecond( float t )
  { 
    float x = _scalar * ( -9 * sin(3*t) );
    float y = _scalar * ( -8 * sin(2*t) - sin(t) );
    float z = _scalar * (  8 * cos(2*t) - cos(t) );
    
    return new PVector( x, y, z ) ;
  }

  //---- Frenet frame
  
  // orthagonal to the normal plane
  private PVector getTangent( float t ) 
  { 
    PVector tangentVector = dtFirst(t);
    tangentVector.normalize();

    return tangentVector;
  }
  
  // orthagonal to the rectifying plane
  private PVector getNormal( float t ) 
  { 
    PVector normalVector = dtFirst(t).cross( dtSecond(t) ).cross( dtFirst(t) );
    // PVector normalVector = dtSecond(t); // NO!
    normalVector.normalize();
    
    return normalVector;
  }

  // orthagonal to the osculating plane
  private PVector getBinormal( float t ) 
  { 
    PVector binormalVector = dtFirst(t).cross( dtSecond(t) );
    binormalVector.normalize();
    
    return binormalVector;
  }

  // center of tube on curve
  private PVector getOrigin( float t ) 
  {
    float x = _scalar * ( sin(3*t) );
    float y = _scalar * ( sin(t) + 2*sin(2*t) );
    float z = _scalar * ( cos(t) - 2*cos(2*t) );

    return new PVector( x, y, z ) ;
  }

  // get points along side of tube
  private PVector[] getPoints( PVector B, PVector N )
  { 
    PVector[] points = new PVector[4];
    
    points[0] = PVector.add( B, N );
    points[3] = PVector.sub( B, N );
    
    // calculate side vector
    PVector side = PVector.sub( points[3], points[0] ); 

    points[1] = PVector.add( points[0], PVector.mult( side, _distance1 ) );
    points[2] = PVector.add( points[0], PVector.mult( side, _distance2 ) );

    return points;
  }
  
  // -----------------
  //  public function
  // -----------------
  
  //  o -- o -- o -- o
  //  |              |
  //  o              o
  //  |     tube     |
  //  o              o
  //  |              |
  //  o -- o -- o -- o
  
  // build vectors for all points on side of tube
  public PVector[] getSide( float theta, float mobius )
  { 
    PVector origin = getOrigin(theta);
    PVector tangent = getTangent(theta);
    PVector[] points = getPoints( getBinormal(theta), getNormal(theta) );
    PVector[] side = new PVector[4];
    PMatrix3D rotationMatrix = new PMatrix3D();
    
    // advance rotation matrix by side iterator plus mobius factor
    rotationMatrix.rotate( mobius, tangent.x, tangent.y, tangent.z ); 
    
    // for each point on side
    for ( int j=0; j<4; j++ )
    {
      side[j] = new PVector();
      // rotate first
      rotationMatrix.mult( points[j], side[j] );
      // then translate
      side[j].add( origin );
    }

    return side;
  }
  
}


  //---- draw graphical user interface
  
  public void drawGUI()
  {
    camera();
    noLights();

    // if there is not an active hover state
    if ( _l == false )
    { 
      // if a mouse button was pressed, lock hover state until mouse button is released
      if ( ( btnCycle.update() | btnRevolve.update() | btnGamut.update() | btnWireframe.update() ) && mousePressed )
      {
        _l = true;
      }
    }
    
    btnCycle.display();
    btnRevolve.display();
    btnGamut.display();
    btnWireframe.display();
  }
  
  //---- io controls

  public void mouseReleased()
  {
    // release lock hover state
    _l = false;
    
    // if a button is pressed, change its state and invert its value
    if ( btnCycle.pressed() )
    {
      _c = 1 - _c; // start/stop cycling
    }
    if ( btnRevolve.pressed() )
    {
      _r = 1 - _r; // start/stop revolution
    }
    if ( btnGamut.pressed() )
    {
      _g = 1 - _g; // show/hide gamut
    }
    if ( btnWireframe.pressed() )
    {
      _w = 1 - _w; // show/hide wireframe
    }
  }
  
  public void keyReleased()
  {
    switch( key ) 
    {
      case 's': 
        _s = _s - 5; // decrease scale
        break;
      case 'S': 
        _s = _s + 5; // increase scale
        break;
      case 'c': 
        _c = 1 - _c; // start/stop cycling
        break;
      case 'r': 
        _r = 1 - _r; // start/stop revolution
        break;
      case 'g': 
        _g = 1 - _g; // show/hide gamut
        break;
      case 'w': 
        _w = 1 - _w; // show/hide wireframe
        break;
    }
  }
  
  //---- gui classes
  
  class Button
  {
    float x, y;
    float w, h;

    String label;
    int clrBase, clrHighlight, clrBorder, clrCurrent;

    boolean depressed = false;

    // constructor
    Button( float xPosition, float yPosition, int btnColor, String btnText ) 
    {
      x = xPosition;
      y = yPosition;
      
      label = btnText;
      
      w = textWidth(label) + 8;
      h = textAscent() + textDescent() + 8;
      
      clrBase = clrCurrent = btnColor;
      clrBorder = color( hue(btnColor), saturation(btnColor), brightness(btnColor)-30 );
      clrHighlight = color( hue(btnColor), saturation(btnColor), brightness(btnColor)+30 );
    }
    
    public void display() 
    {
      stroke( clrBorder );
      strokeWeight( 1 );
      fill( clrCurrent );
      rect( x, y, w, h );
      fill( clrBorder );
      text( label, x + 4, y + h - 6 );
    }
  
    public boolean update() 
    {
      if ( over() )
      {
        clrCurrent = clrHighlight;
        return true;
      } 
      else if ( depressed == false )
      {
        clrCurrent = clrBase;
        return false;
      }
      else
      {
        return false;
      }
    }
  
    public boolean pressed() 
    {
      if ( over() ) 
      {
        if ( depressed == false )
        {
          depressed = true;
          clrCurrent = clrHighlight;
        } 
        else 
        {
          depressed = false;
        }
        return true;
      }
      else
      {
        return false;
      }    
    }
  
    public boolean over() 
    {
      if (
        mouseX >= x && mouseX <= x+w && 
        mouseY >= y && mouseY <= y+h
      ) 
      {
        return true;
      }
      return false;
    }
    
    public float right()
    {
      return x+w;
    }
  }
  public void drawTube( int i, float mobius ) 
  {
    //  --0-- 
    // 1|   |3
    //  --2-- 
    
    int b = ( i + _b ) % _p; // intermediary
    int t = ( i + _u ) % _p; // terminal
    
    // if showing gamut, advance hue interator else show orginal hue
    int h = ( _g == 1 ) ? ceil( 360 - ( i / _u * 5 ) + _h ) % 360 : _o; 
    
    // get vectors for points along side of tube
    PVector[] I = _k.getSide( i * TWO_PI / _p, mobius );
    PVector[] B = _k.getSide( b * TWO_PI / _p, mobius );
    PVector[] T = _k.getSide( t * TWO_PI / _p, mobius );

    // 0___1___2__3   I
    // |___    ___|   A
    // 9   |__|   4   B
    //     7  6
    
    noStroke();
   
    // if not in wireframe mode
    if ( _w != 1 )
    {
      fill( h, 40, 60 );
      
      // if not in gamut mode
      if ( _g != 1 )
      {
        //---- draw sides of tube as mesh with texture
        beginShape(TRIANGLES);
          texture( _t );
    
          vertex( I[0], 0.0f, 0.0f );  //0
          vertex( I[1], _d1, 0.0f );  //1
          vertex( B[1], _d1, 0.5f );  //8
          vertex( B[1], _d1, 0.5f );  //8
          vertex( B[0], 0.0f, 0.5f );  //9
          vertex( I[0], 0.0f, 0.0f );  //0
          
          vertex( I[1], _d1, 0.0f );  //1
          vertex( I[2], _d2, 0.0f );  //2
          vertex( B[2], _d2, 0.5f );  //5
          vertex( B[2], _d2, 0.5f );  //5
          vertex( B[1], _d1, 0.5f );  //8
          vertex( I[1], _d1, 0.0f );  //1
          
          vertex( I[2], _d2, 0.0f );  //2
          vertex( I[3], 1.0f, 0.0f );  //3
          vertex( B[3], 1.0f, 0.5f );  //4
          vertex( B[3], 1.0f, 0.5f );  //4
          vertex( B[2], _d2, 0.5f );  //5
          vertex( I[2], _d2, 0.0f );  //2
         
          vertex( B[1], _d1, 0.5f );  //8
          vertex( B[2], _d2, 0.5f );  //5
          vertex( T[2], _d2, 1.0f );  //6
          vertex( T[2], _d2, 0.95f ); //6
          vertex( T[1], _d1, 0.95f ); //7
          vertex( B[1], _d1, 0.5f );  //8
          
        endShape(CLOSE);
      }
      // in gamut mode
      else
      {
        //---- draw sides of tube as mesh
        beginShape(TRIANGLES);
    
          vertex( I[0] );  //0
          vertex( I[1] );  //1
          vertex( B[1] );  //8
          vertex( B[1] );  //8
          vertex( B[0] );  //9
          vertex( I[0] );  //0
          
          vertex( I[1] );  //1
          vertex( I[2] );  //2
          vertex( B[2] );  //5
          vertex( B[2] );  //5
          vertex( B[1] );  //8
          vertex( I[1] );  //1
          
          vertex( I[2] );  //2
          vertex( I[3] );  //3
          vertex( B[3] );  //4
          vertex( B[3] );  //4
          vertex( B[2] );  //5
          vertex( I[2] );  //2
         
          vertex( B[1] );  //8
          vertex( B[2] );  //5
          vertex( T[2] );  //6
          vertex( T[2] ); //6
          vertex( T[1] ); //7
          vertex( B[1] );  //8
          
        endShape(CLOSE);
      }
      
      strokeWeight( 2 );
      stroke( 0, 0, 12 );
    }
    // in wireframe mode
    else
    {
      strokeWeight( 1 );
      // if not in gamut mode
      if ( _g != 1 )
      {
        stroke( 0, 0, 12 );
      }
      else
      {
        stroke( h, 40, 60 );
      }
    }

    noFill();
    
    //---- draw outline of tube
    
    // 2  __  1  i
    // 3 |__  4  a
    // 3    | 5  b
    beginShape();
      vertex( I[1] );  //1
      vertex( I[0] );  //2
      vertex( B[0] );  //3
      vertex( B[1] );  //4
      vertex( T[1] );  //5
    endShape();

    // 1  __  2 i
    // 4  __| 3 a
    // 5 |      b   
    beginShape();
      vertex( I[2] );  //1
      vertex( I[3] );  //2
      vertex( B[3] );  //3
      vertex( B[2] );  //4
      vertex( T[2] );  //5
    endShape();
  }
  
  // overloaded core functions
  public void vertex( PVector v )
  {
    vertex( v.x, v.y, v.z );
  }
  
  public void vertex( PVector v, float a, float b )
  {
    vertex( v.x, v.y, v.z, a, b );
  }

  static public void main(String args[]) {
    PApplet.main(new String[] { "--present", "--bgcolor=#666666", "--stop-color=#cccccc", "sketch_Eschers_Knot" });
  }
}
