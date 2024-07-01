/*
Contract:

interface ComplexObject {
   aNumber int
   aString string
}

interface Request {
   aNumber int
   aString string
   aObject ComplexObject
}

interface Response {
   aNumber int
   aString string
   aObject ComplexObject
   anArray []ComplexObject
}
*/

exports.handler = async function (event, context) {
    if (!event.aNumber || !event.aString || !event.aObject || !event.aObject.aNumber || !event.aObject.aString) {
      throw new Error('Invalid Input');
    }

    var arr = [];
    arr.push(event.aObject);

    return {
      aNumber: event.aNumber,
      aString: event.aString,
      aObject: event.aObject,
      anArray: arr,
    };
};
