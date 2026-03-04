import { forwardRef, useState } from 'react';
import { Form, Row, Col } from 'react-bootstrap';
import DatePicker, { registerLocale } from 'react-datepicker';
import dayjs from 'dayjs';
import 'react-datepicker/dist/react-datepicker.css';

// Importar español
import es from 'date-fns/locale/es';
registerLocale('es', es);

export default function DateRangePicker({ fechaInicio, fechaMaxima, value, setValue }) {

    return (
        <>
            <DatePicker
                locale="es"
                selectsRange
                startDate={fechaInicio}
                endDate={value}
                onChange={(dates) => {
                    setValue(dates[1] ?? dates[0])
                }}
                minDate={fechaInicio}
                maxDate={fechaMaxima}
                disabled={!fechaInicio}
                dateFormat="dd/MM/yyyy"
                className="w-100 form-control"
                customInput={<CustomInput endDate={value} />}
                placeholderText="dd/mm/aaaa"
            />
        </>
    );
}

const CustomInput = forwardRef(({ endDate, onClick }, ref) => {

    return < Form.Control
        type="text"
        onClick={onClick}
        ref={ref}
        value={endDate ? dayjs(endDate).format('DD/MM/YYYY') : ''}
        placeholder="dd/mm/aaaa"
        readOnly
    />
});