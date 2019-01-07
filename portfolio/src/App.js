import React from 'react'
import { Collapse, Jumbotron, Modal, ModalHeader, ModalBody, Navbar, NavbarToggler, NavbarBrand } from 'reactstrap'
import KonvaGraphics from './RoundTstat.js'
import Sudoku from './Sudoku.js'

import './App.css'

import arb from './img/arb.png'
import comingSoon from './img/coming-soon.jpg'
import dgbox from './img/dgbox.jpg'
import hackerrank from './img/hackerrank.png'
import konvaGraphics from './img/konva-graphics.png'
import manfredHero from './img/manfred-hero.jpg'
import manfredGuts from './img/manfred-guts.jpg'
import manfredUltra from './img/manfred-ultra.jpg'
import mannabase from './img/mannabase.png'
import puzzle from './img/puzzle.png'
import reflection from './img/espejo-infinito_619.jpg'
import rocksHero from './img/rocks-hero.jpg'
import rocksWide from './img/rocks-wide.jpg'
import rocksTall from './img/rocks-tall.jpg'
import sudoku from './img/sudoku.jpg'
import worm from './img/worm.png'

export default class App extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      collapsed: true,
      konvaModalOpen: false,
      manfredModalOpen: false,
      rocksModalOpen: false,
      sudokuModalOpen: false,
    };

    this.toggle = this.toggle.bind(this);
  }

  toggle(key) {
    const state = {};
    state[key] = !this.state[key];
    this.setState(state);
  }

  render() {
    const collapsedToggle = () => this.toggle('collapsed');
    const konvaToggle = () => this.toggle('konvaModalOpen');
    const manfredToggle = () => this.toggle('manfredModalOpen');
    const rocksToggle = () => this.toggle('rocksModalOpen');
    const sudokuToggle = () => this.toggle('sudokuModalOpen');

    return (
      <div>
        <header>
          <Navbar color="dark" className="navbar-dark">
            <Collapse isOpen={!this.state.collapsed} navbar>
              <div className="container">
                <div className="row">
                  <div className="col-sm-8 py-4">
                    <h4 className="text-white">About</h4>
                    <p className="text-muted">
                      I'm mostly a software developer, but I can't seem to keep out of other stuff. I&apos;ve been programming in
                      Java pretty much since it came out, but I also work with node.js, ReactJs, Python, Clojure and all of
                      the bits that go along with those things. I studied math at Waterloo, and so I have a yen for data
                      science and AI as well. Oh, and I also got into amateur electronics (Arduino mostly) for a while.
                      Finally, I do a lot of freelance work, but it always seems that whenever I&apos;m on a team I end up in a
                      leadership role. Not sure how that keeps happening. One thing I am not is a graphic designer. If
                      anything I work on approaches visual beauty, it is entirely coincidental.
                    </p>
                  </div>
                  <div className="col-sm-4 py-4">
                    <h4 className="text-white">Contact</h4>
                    <ul className="list-unstyled">
                      <li><a href="https://www.linkedin.com/in/matthew-lohbihler" className="text-white">Find on LinkedIn</a></li>
                      <li><a href="mailto:matthew@serotonin.ai" className="text-white">matthew@serotonin.ai</a></li>
                      <li><a href="#" className="text-white">647.405.0599</a></li>
                    </ul>
                  </div>
                </div>
              </div>
            </Collapse>

            <div className="container d-flex justify-content-between text-white">
              <NavbarBrand className="mr-auto">Matthew A. Lohbihler</NavbarBrand>
              <NavbarToggler onClick={collapsedToggle} className="mr-2"/>
            </div>
          </Navbar>
        </header>

        <main role="main">
          <Jumbotron className="text-center">
            <div className="container">
              <h1 className="jumbotron-heading">Portfolio</h1>
              <p className="lead text-muted">
                Interviews are too short to describe all of the stuff that I&apos;ve worked on. And trying to put it all in my
                resume would be plain old silly. So, here is a portfolio of some of my (mostly) non-professional work, at least the parts of it that I can show.
                It&apos;s still incomplete, but it&apos;s a start. If you want to chat about anything, click the menu button above for my coordinates.
              </p>
            </div>
          </Jumbotron>

          <div className="album text-muted">
            <div className="container">
              <div className="row">
                <div className="card">
                  <a href="https://mannabase.com/030B04010A01020C03020806030902010D0F030804010C0102240701080502460D.html">
                    <img src={puzzle} alt="Puzzle" style={{padding: "0px 0px"}}/>
                  </a>
                  <p className="card-text">
                    2019 - After watching <a target="_blank" rel="noopener noreferrer" href="https://www.ted.com/talks/alex_rosenthal_the_joyful_perplexing_world_of_puzzle_hunts">Alex 
                    Rosenthal&apos;s TED talk</a> on the MIT puzzlehunt, I was hopelessly inspired. I then spent the better part of two weeks conceiving,
                    creating, and deploying my own Manna-branded puzzle. As of the time of writing it is still being tested, but I will update here with
                    any interesting outcomes. In the meantime, why
                    not <a href="https://mannabase.com/030B04010A01020C03020806030902010D0F030804010C0102240701080502460D.html">try it yourself</a>?
                  </p>
                </div>

                <div className="card">
                  <img src={arb} alt="Crpto arbitrage" style={{padding: "50px 0px"}}/>
                  <p className="card-text">
                    2018 - I literally woke up in the middle of the night with this realization; crypto exchanges are arbitrage opportunities. Any exchange
                    where loops can be created through 3 or more markets would work. And with the thousands of exchanges there are in the world, there might
                    actually be some money to be made. Well, that turned out to be true, and even better, pretty much every exchange there is offers an API
                    where this can be automated. Which I did. And no, you can&apos;t have the source code. But I will say that I have 7 EC2 instances running
                    it, and the software easily pays for them all.
                  </p>
                </div>

                <div className="card">
                  <img src={konvaGraphics} alt="Konva graphics" onClick={konvaToggle} className="pointer"/>
                  <p className="card-text">2017 - Interactive canvas graphics built with Konva and ReactKonva</p>

                  <Modal isOpen={this.state.konvaModalOpen} toggle={konvaToggle} className="modal-lg" autoFocus={false}>
                    <ModalHeader toggle={konvaToggle}>Canvas graphics with Konva</ModalHeader>
                    <ModalBody className="konva-model-body">
                      <div id="konva-tstat-container"><KonvaGraphics/></div>
                      This is a thermostat controller with both heating and cooling setpoints. The locations of the hot
                      and cold handles are randomly initialized. You can grab and drag them around to set your setpoints.
                      This was created for a BACnet panel app that proved to be too risky to create due to hardware and
                      sales uncertainties. The product had a ReactJS client which worked with a Java/Tomcat API server.
                      The whole thing ran on a Raspberry Pi with a 7&quot; touch screen.
                    </ModalBody>
                  </Modal>
                </div>

                <div className="card">
                  <a href="http://www.mannabase.com/">
                    <img src={mannabase} alt="Mannabase logo" style={{padding: "74px 0px"}}/>
                  </a>
                  <p className="card-text">
                    2017 - I am now part of the team helping to build Mannabase (nee Grantcoin), a blockchain implementation
                    of a worldwide Universal Basic Income. It&apos;s a great idea, a great cause, by great people, and you should sign up. Now. NOW!!!
                    Contact me to ask more about this great project.
                  </p>
                </div>

                <div className="card">
                  <a href="https://www.hackerrank.com/mlohbihler">
                    <img src={hackerrank} alt="HackerRank logo"/>
                  </a>
                  <p className="card-text">
                    Check out my algorithms ranking on HackerRank! I&apos;m also ranked #1 in the Java category ... along with 798 other people.
                  </p>
                </div>

                <div className="card">
                  <img src={manfredHero} alt="Manfred" onClick={manfredToggle} className="pointer"/>
                  <p className="card-text">
                    2016 - Becoming more interested in electronics, I built a flight data recorder for a remote controlled airplane.
                  </p>

                  <Modal isOpen={this.state.manfredModalOpen} toggle={manfredToggle} className="modal-lg" autoFocus={false}>
                    <ModalHeader toggle={manfredToggle}>Flight data recorder</ModalHeader>
                    <ModalBody className="konva-model-body">
                      <p>Code for this project can be found <a href="https://github.com/mlohbihler/manfred">here</a>.</p>
                      <p>
                        I&apos;ve always had a fascination for remote controlled stuff, especially airplanes. And so, when I finally had an excuse to buy one it was a bit of a dream come true.
                        This project was intended to be a flight data recorder - like a black box on regular airplane, but smaller. When I was finally done, this is what it looked like:
                      </p>
                      <p><img src={manfredGuts} alt="Guts"/></p>
                      <p>
                        All of this plugged into a Raspberry Pi that was also placed into the payload area. Everything ran off of the airplane battery. Sensor input included:
                      </p>
                      <ul>
                        <li>Servos for rudder, elevator, ailerons, and throttle</li>
                        <li>GPS receiver</li>
                        <li>Accelerometer/gyroscope</li>
                        <li>Ultrasound distance sensor (useful during takeoff and landing, pictured below)</li>
                      </ul>
                      <p>
                        The Arduino Nano was needed for its analog and digital inputs. It communicated with the accelerometer via I2C, and with the RaspPi via RS232. The GPS was connected directly to
                        the RaspPi, also with RS232. Data was written to a TinyTSDB database on the RaspPi. Operating state was signalled through the LEDs using colour and blinking patterns.
                      </p>
                      <p>This is the ultrasound distance sensor, mounted to the bottom of the plane:</p>
                      <p><img src={manfredUltra} alt="Ultrasound"/></p>
                    </ModalBody>
                  </Modal>
                </div>

                <div className="card">
                  <img src={sudoku} alt="Sudoku" style={{padding: "0px 37px"}} onClick={sudokuToggle} className="pointer"/>
                  <p className="card-text">
                    2005 - I jumped on the Sudoku bandwagon around the time that they came out. It&apos;s a great puzzle, and an even greater way to waste time. Eventually, like
                    most other things, I decided it would be much more convenient for a computer to solve them for me, so I created a solver.
                  </p>

                  <Modal isOpen={this.state.sudokuModalOpen} toggle={sudokuToggle} className="modal-lg" autoFocus={false}>
                    <ModalHeader toggle={sudokuToggle}>Sudoku solver</ModalHeader>
                    <ModalBody>
                      <p>
                        This project was originally written in Java, but lacked a UI and so was ported to React for showcasing here. The original in addition to 9x9 models
                        also allowed for 4x4, 16x16, and 25x25 models. Code for this project can be found <a href="https://github.com/mlohbihler/various/tree/master/Sudoku">here</a>.
                      </p>
                      <p>
                        The default puzzle that is shown was rated &quot;diabolical&quot;, and it&apos;s pretty tough to solve by hand. The solver does it easily
                        though. The same can happily be said for the so-called world&apos;s hardest sudoku. I had to make some small changes to the code to handle it &mdash; it
                        was designed after I wrote this code I think &mdash; but it gets solved just fine.
                      </p>
                      <p>
                        The puzzle is editable, so go ahead and enter the one from today&apos;s paper.
                      </p>
                      <Sudoku/>
                    </ModalBody>
                  </Modal>
                </div>

                <div className="card">
                  <img src={rocksHero} alt="Climbing wall" style={{padding: "40px 0px"}} onClick={rocksToggle} className="pointer"/>
                  <p className="card-text">2017 - Ok, this isn&apos;t a tech project, but it was fun anyway. I was allocated a bit of space in the basement by my wife to build a rock climbing wall.</p>

                  <Modal isOpen={this.state.rocksModalOpen} toggle={rocksToggle} className="modal-lg" autoFocus={false}>
                    <ModalHeader toggle={rocksToggle}>Basement rock-climbing wall</ModalHeader>
                    <ModalBody className="konva-model-body">
                      <p>
                        It includes a 15&deg; incline wall, a flat wall, ceiling holds, a campus wall, and LED lighting around the top. (It&apos;s in a basement after all.) There are also some cleverly placed holds on the side of the incline.
                        The kids love to swing from the campus holds.
                      </p>
                      <p><img src={rocksWide} alt="Wide shot"/></p>
                      <p><img src={rocksTall} alt="Tall shot"/></p>
                    </ModalBody>
                  </Modal>
                </div>

                <div className="card">
                  <a href="https://github.com/mlohbihler/various/tree/master/portfolio">
                    <img src={reflection} alt="Reflection" style={{padding: "0px 64px"}}/>
                  </a>
                  <p className="card-text">
                    2017 - Are you itching to see how this site was created? I&apos;m not one to keep good people like yourself in suspense.
                    This page is hosted on GitHub at https://mlohbihler.github.io. There are easy-to-follow instructions there about how to
                    set one up. All of the code is kept <a href="https://github.com/mlohbihler/various/tree/master/portfolio">here</a>. The
                    only part that I don&apos;t like about how it works is the deployment. Once I have the code the way I want I run a
                    "yarn build" to create the build directory. Then I copy the contents of that directory to my git workspace for
                    mlohbiher.github.io, and commit and push from there. It would be much nicer to be able to build and deploy directly from
                    the source repo, but github.io doesn&apos;t want to work that way.
                  </p>
                </div>

                <div className="card">
                  <a href="https://www.youtube.com/watch?v=K5DcK-ImFxc">
                    <img src={worm} alt="JBox2D worm" style={{padding: "35px 13px"}}/>
                  </a>
                  <p className="card-text">
                    2015 - This was some AI experimentation that I was doing. I was interested in animal movement, believing it
                    to be the reason for nervous systems, and so the basic of intelligence. I had to create
                    my own MuscleJoint classes to simulate muscle contractions, and it works really well for a while but, as you can see
                    in <a href="https://www.youtube.com/watch?v=K5DcK-ImFxc">the video</a>, it eventually doesn&apos;t quite work
                    as intended. Still, it&apos;s always fun to play with JBox.
                  </p>
                </div>

                <div className="card">
                  <a href="http://www.dglogik.com/company/iot-news/146-2013-cta-winner-in-two-categories">
                    <img src={dgbox} alt="DGBox" style={{padding: "51px 35px"}}/>
                  </a>
                  <p className="card-text">
                    2013 - This was paid work, but it just turned out so nice. I worked independently with DGLogik over the course of a year to create
                    DGBox, a building controls appliance. It was based upon Mango, and customized for use in a dedicted Beaglebone platform. It won
                    the <a href="http://www.dglogik.com/company/iot-news/146-2013-cta-winner-in-two-categories">ControlTrends Awards Commercial Product
                    of the Year</a>.
                  </p>
                </div>

                <div className="card">
                  <img src={comingSoon} alt="Coming soon"/>
                  <p className="card-text">
                    There&apos;s quite a bit more to show, including alexa skills, more electronics, blockchains, tiling software, utilities, patents, ...
                    As soon as there&apos;s a minute, I&apos;ll put that stuff up too.
                  </p>
                </div>


                {/*
                  - nullstat
                  - TinyTSDB
                  - sero-warp
                  - sero-scheduler
                  - glacier
                  - CG patent
                  - Amazon stuff?
                  - J2 connectors?
                  - BACnet4J
                  - wheeljack?
                  - Cadex
                  - interlock/jtile
                  - cluster builder
                  - backsplash
                  - Mango
                  - Signamo?


                <div className="card">
                  <img data-src="holder.js/100px280?theme=thumb" alt="Card image cap"/>
                  <p className="card-text">This is a wider card with supporting text below as a natural lead-in to additional content. This content is a little bit longer.</p>
                </div>
                <div className="card">
                  <img data-src="holder.js/100px280?theme=thumb" alt="Card image cap"/>
                  <p className="card-text">This is a wider card with supporting text below as a natural lead-in to additional content. This content is a little bit longer.</p>
                </div>
                */}
              </div>
            </div>
          </div>
        </main>

        <footer className="text-muted">
          <div className="container">
            <p className="float-right">
              <a href="#">Back to top</a>
            </p>
            <p>Copyright &copy; 2017 Matthew A. Lohbihler</p>
          </div>
        </footer>
      </div>
    );
  }
}
