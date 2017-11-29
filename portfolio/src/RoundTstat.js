import React, { Component } from 'react';
import {Arc, Circle, Group, Layer, Stage, Text} from 'react-konva';
import PropTypes from 'prop-types'

const size = 400 // Size of the drawing
const padding = 25 // Inset around the drawing
const ringRadius = 40 // The thickness of the handle ring
const minAngle = 6 // The minimum angle of setpoint dials
const maxAngle = 174 // The maximum angle of setpoint dials

// Heat constants
const heatMin = 15
const heatMax = 30

// Cool constants
const coolMin = 15
const coolMax = 30

const styles = {
  floater: {
    float: 'left',
    color: 'black',
  },
  setPoints: {
    marginTop: '10px',
    textAlign: 'center',
  },
  box: {
    border: "solid 1px black",
    padding: "10px",
  },
  title: {
    marginTop: '5px',
    fontSize: '1.6em',
  },
  innerTitle: {
    fontSize: '1.3em',
  },
  value: {
    fontSize: '2em',
    fontWeight: "bold",
  },
};

const prettyTemp = (temp) => {
  return (Math.round(temp * 2) / 2).toFixed(1)
}

export default class RoundTstat extends Component {
  state = {
    heatSetpoint: 22,
    coolSetpoint: 22,
    temperature: null,
    humidity: null,
    outside: null,
    timeoutId: null,
  }

  componentDidMount() {
    this.setState({
      heatSetpoint: Math.random() * 9 + 18,
      coolSetpoint: Math.random() * 9 + 18,
      timeoutId: setTimeout(() => this.update(), 1000)
    })
  }

  componentWillUnmount() {
    clearTimeout(this.state.timeoutId)
  }

  update() {
    this.setState({
      temperature: prettyTemp(Math.random() * 4 + 20),
      humidity: Math.round((Math.random() * 4 + 20)),
      outside: prettyTemp(Math.random() * 4 + 28),
      timeoutId: setTimeout(() => this.update(), 3000)
    })
  }

  render() {
    const scale = 1

    const humidity = this.state.humidity ? this.state.humidity +"%" : "--"
    const outside = this.state.outside ? this.state.outside +"\u00B0C" : "--"

    return (
      <div>
        <div style={styles.floater}>
          <Stage width={size + padding * 2} height={size + padding * 2}>
            <Layer x={padding} y={padding} scale={{ x: scale, y: scale }}>
              <Drawing scale={scale}
                       changeHeat={(sp) => this.setState({ heatSetpoint: sp })}
                       changeCool={(sp) => this.setState({ coolSetpoint: sp })}
                       heatSetpoint={this.state.heatSetpoint}
                       coolSetpoint={this.state.coolSetpoint}
                       temperature={this.state.temperature}/>
            </Layer>
          </Stage>
        </div>
        <div style={{...styles.floater, ...styles.setPoints, transform: "scale(scale, scale)"}}>
          <div style={styles.title}>Setpoints</div>
          <div style={styles.box}>
            <div style={styles.innerTitle}>Cooling</div>
            <div style={styles.value}>{prettyTemp(this.state.coolSetpoint)}&deg;C</div>
            <div style={styles.innerTitle}>Heating</div>
            <div style={styles.value}>{prettyTemp(this.state.heatSetpoint)}&deg;C</div>
          </div>
          <div style={styles.title}>Humidity</div>
          <div style={styles.box}>
            <div style={styles.value}>{humidity}</div>
          </div>
          <div style={styles.title}>Outside Air</div>
          <div style={styles.box}>
            <div style={styles.value}>{outside}</div>
          </div>
        </div>
      </div>
    );
  }
}

RoundTstat.propTypes = {
};

class Drawing extends Component {
  onDragMove(drag) {
    // y is the scaled location of the pointer relative to the stage.
    let y = drag.target.getStage().getPointerPosition().y
    const yMin = (padding + ringRadius / 2) * this.props.scale
    const yMax = (padding + size - ringRadius / 2) * this.props.scale

    // Constrain the y value.
    y = (y > yMax) ? yMax : (y < yMin) ? yMin : y

    // Convert to number between -1 and 1.
    let temp = 1 - ((y - yMin) / (yMax - yMin) * 2)

    // Convert to an angle between 0 and 180
    temp = (Math.asin(temp) / Math.PI + 0.5) * 180

    // Constrain to angle limits
    temp = (temp > maxAngle) ? maxAngle : (temp < minAngle) ? minAngle : temp

    // Convert to a set point
    temp = (temp - minAngle) / (maxAngle - minAngle)

    if (drag.target.id() === "heat") {
      temp = temp * (heatMax - heatMin) + heatMin
      this.props.changeHeat(temp)
    } else {
      temp = temp * (coolMax - coolMin) + coolMin
      this.props.changeCool(temp)
    }
  }

  render() {
    // Translate the heat value to an angle.
    let heatAngle = (this.props.heatSetpoint - heatMin) / (heatMax - heatMin)
    heatAngle = heatAngle * (maxAngle - minAngle) + minAngle

    let coolAngle = (this.props.coolSetpoint - coolMin) / (coolMax - coolMin)
    coolAngle = coolAngle * (maxAngle - minAngle) + minAngle

    let temperature = ""
    if (this.props.temperature) {
      temperature = this.props.temperature +"\u00B0C"
    }

    return (
      <Group x={size / 2} y={size / 2}
             onDragMove={(e) => this.onDragMove(e)}>
        <Arc angle={heatAngle} rotation={90}
             innerRadius={size / 2 - ringRadius} outerRadius={size / 2}
             stroke="gray"
             fill={"red"}
        />
        <Arc angle={180 - heatAngle} rotation={90 + heatAngle}
             innerRadius={size / 2 - ringRadius} outerRadius={size / 2}
             stroke="gray"
             fill={"lightGray"}
        />
        <Arc clockwise="false"
             angle={360 - coolAngle} rotation={90}
             innerRadius={size / 2 - ringRadius} outerRadius={size / 2}
             stroke="gray"
             fill={"blue"}
        />
        <Arc clockwise="false"
             angle={180 + coolAngle} rotation={450 - coolAngle}
             innerRadius={size / 2 - ringRadius} outerRadius={size / 2}
             stroke="gray"
             fill={"lightGray"}
        />
        <Text x={-size / 2} y={-size / 8}
          align={"center"}
          fontFamily={"Calibri"}
          fontSize={size / 4}
          text={temperature}
          width={size}
        />
        <Circle id="heat" radius={ringRadius / 2 - 4} x={0} y={0}
             fill={"white"}
             stroke={"gray"}
             offsetY={(size - ringRadius) / 2}
             rotation={heatAngle - 180}
             draggable={true}
        />
        <Circle id="cool" radius={ringRadius / 2 - 4} x={0} y={0}
             fill={"white"}
             stroke={"gray"}
             offsetY={(size - ringRadius) / 2}
             rotation={180 - coolAngle}
             draggable={true}
        />
      </Group>
    )
  }
}

Drawing.propTypes = {
  scale: PropTypes.number.isRequired,
  changeHeat: PropTypes.func.isRequired,
  changeCool: PropTypes.func.isRequired,
  heatSetpoint: PropTypes.number.isRequired,
  coolSetpoint: PropTypes.number.isRequired,
  temperature: PropTypes.string,
};
